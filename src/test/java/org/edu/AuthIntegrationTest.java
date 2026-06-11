package org.edu;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.dto.request.LoginRequest;
import org.edu.dto.request.UserRegistrationRequest;
import org.edu.util.Role;
import org.edu.repository.BlacklistedTokenRepository;
import org.edu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        blacklistedTokenRepository.deleteAll();
        userRepository.deleteAll();
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setName("Alice Admin");
        registrationRequest.setEmail("alice@example.com");
        registrationRequest.setPassword("Password123");
        registrationRequest.setRole(Role.ADMIN);
    }

    @Test
    void shouldRegisterUser() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email", is("alice@example.com")))
            .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void shouldReturnStructuredFieldErrorsForInvalidRegistration() throws Exception {
        registrationRequest.setName("");
        registrationRequest.setEmail("not-an-email");
        registrationRequest.setPassword("short");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Bad Request")))
            .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
            .andExpect(jsonPath("$.message", is("Request validation failed")))
            .andExpect(jsonPath("$.path", is("/api/users")))
            .andExpect(jsonPath("$.fieldErrors.name", is("Name is required")))
            .andExpect(jsonPath("$.fieldErrors.email", is("Email must be valid")))
            .andExpect(jsonPath("$.fieldErrors.password", is("Password must be between 8 and 100 characters")));
    }

    @Test
    void shouldReturnStructuredErrorForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Alice\""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")))
            .andExpect(jsonPath("$.message", is("Request body is malformed")))
            .andExpect(jsonPath("$.path", is("/api/users")));
    }

    @Test
    void shouldEnforceEightCharacterLoginPasswordValidation() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("1234567");

        mockMvc.perform(post("/api/auth/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
            .andExpect(jsonPath("$.fieldErrors.password", is("Password must be between 8 and 100 characters")));
    }

    @Test
    void shouldLoginAndFetchCurrentUser() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
            .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType", is("Bearer")))
            .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    void shouldLogoutAndInvalidateToken() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
            .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("alice@example.com");
        loginRequest.setPassword("Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(delete("/api/auth/tokens")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Logout successful")));

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
            .andExpect(jsonPath("$.message", is("Unauthorized access")))
            .andExpect(jsonPath("$.path", is("/api/users/me")));
    }
}
