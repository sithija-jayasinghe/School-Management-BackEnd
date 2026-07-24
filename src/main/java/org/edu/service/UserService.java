package org.edu.service;

import org.edu.dto.request.UserUpdateRequest;
import org.edu.dto.request.UserRegistrationRequest;
import org.edu.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    List<UserResponse> getActiveUsers();

    List<UserResponse> getInactiveUsers();

    UserResponse getUserById(Long userId);

    UserResponse updateUser(Long userId, UserUpdateRequest request);

    void activateUser(Long userId);

    void deactivateUser(Long userId);

    UserResponse getCurrentUser();
}
