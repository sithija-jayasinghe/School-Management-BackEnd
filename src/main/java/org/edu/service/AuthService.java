package org.edu.service;

import org.edu.dto.request.LoginRequest;
import org.edu.dto.response.AuthTokenResponse;
import org.edu.dto.response.MessageResponse;

public interface AuthService {

    AuthTokenResponse login(LoginRequest request);

    MessageResponse logout(String authorizationHeader);
}
