package org.example.thuan_security.service;

import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.UserResponse;

public interface UserService {
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse validateLoginWithOtp(String email, String otp);

    ApiResponse register(RegisterRequest registerRequest);
    UserResponse getUserInfo(String token);
    UserResponse updateUserInfo(String token, UserResponse userResponse);

    boolean isVerifiedAccount(String email);

    ApiResponse changePassword(ChangePasswordRequest changePasswordRequest,String email);

    ApiResponse sendMailForgotPassword(String email);

    ApiResponse changeForgotPassword(String email, String otp, String newPassword);
}
