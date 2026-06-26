package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.edu.dto.request.LoginRequest;
import org.edu.dto.response.AuthTokenResponse;
import org.edu.dto.response.MessageResponse;
import org.edu.dto.response.UserResponse;
import org.edu.util.Role;
import org.edu.entity.User;
import org.edu.exception.InvalidCredentialsException;
import org.edu.exception.UnauthorizedException;
import org.edu.repository.UserRepository;
import org.edu.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("Password123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("hashed-password");
        user.setRole(Role.STUDENT);
        user.setActive(true);

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail("john@example.com");
        userResponse.setRole(Role.STUDENT);
        userResponse.setActive(true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.extractExpiration("jwt-token")).thenReturn(Instant.now().plusSeconds(3600));
        when(userService.toResponse(user)).thenReturn(userResponse);

        AuthTokenResponse response = authService.login(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(response.getExpiresIn() > 0);
        assertEquals("john@example.com", response.getUser().getEmail());
    }

    @Test
    void shouldRejectInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong-password");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("hashed-password");
        user.setActive(true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldRejectInactiveUserLogin() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("Password123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("hashed-password");
        user.setActive(false);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed-password")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldBlacklistTokenOnLogout() {
        when(tokenBlacklistService.isBlacklisted("jwt-token")).thenReturn(false);
        when(jwtService.extractExpiration("jwt-token")).thenReturn(Instant.now().plusSeconds(3600));

        MessageResponse response = authService.logout("Bearer jwt-token");

        assertEquals("Logout successful", response.getMessage());
        verify(tokenBlacklistService).blacklist(any(), any());
    }

    @Test
    void shouldRejectMissingAuthorizationHeader() {
        assertThrows(UnauthorizedException.class, () -> authService.logout(null));
    }
}
