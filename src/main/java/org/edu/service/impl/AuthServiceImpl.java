package org.edu.service.impl;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.edu.dto.request.LoginRequest;
import org.edu.dto.response.AuthTokenResponse;
import org.edu.dto.response.MessageResponse;
import org.edu.entity.User;
import org.edu.exception.InvalidCredentialsException;
import org.edu.exception.UnauthorizedException;
import org.edu.repository.UserRepository;
import org.edu.security.JwtService;
import org.edu.service.AuthService;
import org.edu.service.TokenBlacklistService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserServiceImpl userService;

    @Override
    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new AuthTokenResponse(
            token,
            "Bearer",
            Duration.between(Instant.now(), jwtService.extractExpiration(token)).toMillis(),
            userService.toResponse(user)
        );
    }

    @Override
    public MessageResponse logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (tokenBlacklistService.isBlacklisted(token)) {
            return new MessageResponse("Token already invalidated");
        }
        tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
        return new MessageResponse("Logout successful");
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Authorization header is missing or invalid");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
