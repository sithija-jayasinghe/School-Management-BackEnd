package org.edu;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.edu.dto.request.LoginRequest;
import org.edu.dto.request.SystemSettingsUpdateRequest;
import org.edu.entity.User;
import org.edu.repository.AuditLogRepository;
import org.edu.repository.BlacklistedTokenRepository;
import org.edu.repository.SystemSettingsRepository;
import org.edu.repository.UserRepository;
import org.edu.util.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemSettingsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SystemSettingsRepository systemSettingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        blacklistedTokenRepository.deleteAll();
        auditLogRepository.deleteAll();
        systemSettingsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnPersistedDefaultSettingsForAdmin() throws Exception {
        String token = issueToken("admin@sms.lk", Role.ADMIN);

        mockMvc.perform(get("/api/system-settings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.defaultLanguage", is("en")))
            .andExpect(jsonPath("$.timeZone", is("Asia/Colombo")));
    }

    @Test
    void shouldUpdateSystemSettingsForAdmin() throws Exception {
        String token = issueToken("admin@sms.lk", Role.ADMIN);
        SystemSettingsUpdateRequest request = new SystemSettingsUpdateRequest();
        request.setSchoolName("Central College Kandy");
        request.setPhoneNumber("0771234567");
        request.setTimeZone("Asia/Colombo");

        mockMvc.perform(patch("/api/system-settings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.schoolName", is("Central College Kandy")))
            .andExpect(jsonPath("$.phoneNumber", is("0771234567")));
    }

    @Test
    void shouldRejectTeacherAccessToSystemSettings() throws Exception {
        String token = issueToken("teacher@sms.lk", Role.TEACHER);

        mockMvc.perform(get("/api/system-settings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));
    }

    private String issueToken(String email, Role role) throws Exception {
        User user = new User();
        user.setName("System User");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
