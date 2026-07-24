package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.request.LoginRequest;
import org.edu.dto.response.AuthTokenResponse;
import org.edu.dto.response.MessageResponse;
import org.edu.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/tokens")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT authentication and session token operations")
public class AuthController {

    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Sign in and issue a JWT access token")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @DeleteMapping
    @Operation(summary = "Sign out and blacklist the current JWT", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MessageResponse> logout(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        return ResponseEntity.ok(authService.logout(authorizationHeader));
    }
}
