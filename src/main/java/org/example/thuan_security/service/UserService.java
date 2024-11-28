package org.example.thuan_security.service;

import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.*;

public interface UserService {
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse validateLoginWithOtp(String email, String otp);

    UserKCLResponse register(RegisterRequest registerRequest);

    UserResponse getUserInfo(String token);

    ApiResponse updateUserInfo(String token, UserResponse userResponse);

    boolean isVerifiedAccount(String email);

    ApiResponse changePassword(ChangePasswordRequest changePasswordRequest,String email);

    ApiResponse sendMailForgotPassword(String email);

    ApiResponse changeForgotPassword(String email, String otp, String newPassword);

    String createUser(RegisterRequest registerRequest);
}
