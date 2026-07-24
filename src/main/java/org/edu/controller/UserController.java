package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.request.UserRegistrationRequest;
import org.edu.dto.request.UserUpdateRequest;
import org.edu.dto.response.UserResponse;
import org.edu.service.AuditLogService;
import org.edu.service.UserService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration and current-user profile access")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new system user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.register(request);
        auditLogService.log(
            AuditAction.CREATE,
            AuditEntityType.USER,
            response.getId(),
            response.getName(),
            "Created " + response.getRole() + " user account"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List system users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users by name or email", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam String keyword, Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(keyword, pageable));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List active users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List inactive users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserResponse>> getInactiveUsers() {
        return ResponseEntity.ok(userService.getInactiveUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a user by id", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        auditLogService.log(
            AuditAction.UPDATE,
            AuditEntityType.USER,
            response.getId(),
            response.getName(),
            "Updated user profile and role assignment"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        String userName = userService.getUserById(userId).getName();
        userService.activateUser(userId);
        auditLogService.log(
            AuditAction.ACTIVATE,
            AuditEntityType.USER,
            userId,
            userName,
            "Activated user account"
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        String userName = userService.getUserById(userId).getName();
        userService.deactivateUser(userId);
        auditLogService.log(
            AuditAction.DEACTIVATE,
            AuditEntityType.USER,
            userId,
            userName,
            "Deactivated user account"
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }
}
