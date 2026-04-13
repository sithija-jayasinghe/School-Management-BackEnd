package org.edu.service;

import org.edu.dto.request.UserRegistrationRequest;
import org.edu.dto.response.UserResponse;

public interface UserService {

    UserResponse register(UserRegistrationRequest request);

    UserResponse getCurrentUser();
}
