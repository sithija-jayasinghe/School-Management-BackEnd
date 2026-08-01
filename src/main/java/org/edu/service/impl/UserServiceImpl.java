package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.request.UserRegistrationRequest;
import org.edu.dto.request.UserUpdateRequest;
import org.edu.dto.response.UserResponse;
import org.edu.entity.User;
import org.edu.exception.DuplicateEmailException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.filter.FilterSpecifications;
import org.edu.filter.UserFilterDefinitions;
import org.edu.repository.UserRepository;
import org.edu.security.UserPrincipal;
import org.edu.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(UserRegistrationRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> filterUsers(Map<String, String> filters, Pageable pageable) {
        return userRepository.findAll(FilterSpecifications.build(filters, UserFilterDefinitions.definitions()), pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        return userRepository
            .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword.trim(), keyword.trim(), pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByActiveTrueOrderByNameAsc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getInactiveUsers() {
        return userRepository.findByActiveFalseOrderByNameAsc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return toResponse(getUserEntityById(userId));
    }

    @Override
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = getUserEntityById(userId);
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));
    }

    @Override
    public void activateUser(Long userId) {
        User user = getUserEntityById(userId);

        if (user.isActive()) {
            throw new IllegalStateException("User already active");
        }

        user.setActive(true);
    }

    @Override
    public void deactivateUser(Long userId, Long authenticatedUserId) {
        if (userId.equals(authenticatedUserId)) {
            throw new IllegalStateException("You cannot deactivate your own account");
        }

        User user = getUserEntityById(userId);

        if (!user.isActive()) {
            throw new IllegalStateException("User already inactive");
        }

        user.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return toResponse(userPrincipal.getUser());
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
