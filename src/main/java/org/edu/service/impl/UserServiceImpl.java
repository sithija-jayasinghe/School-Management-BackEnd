package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.request.UserRegistrationRequest;
import org.edu.dto.response.UserResponse;
import org.edu.entity.User;
import org.edu.exception.DuplicateEmailException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.UserRepository;
import org.edu.security.UserPrincipal;
import org.edu.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));
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
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
