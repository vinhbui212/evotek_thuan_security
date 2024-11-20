package org.example.thuan_security.service;

import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.UserResponse;

public interface UserService {
    LoginResponse login(LoginRequest loginRequest);
    ApiResponse register(RegisterRequest registerRequest);
//    UserResponse getUserInfo();
}
