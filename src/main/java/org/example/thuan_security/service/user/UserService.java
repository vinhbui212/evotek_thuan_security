package org.example.thuan_security.service.user;

import org.example.thuan_security.model.Users;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.request.ResetPasswordRequest;
import org.example.thuan_security.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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

     String updateEnabled(String id, RegisterRequest request);

    String resetPassword( String userId,  ResetPasswordRequest request);

    Page<Users> getAllUsers(Pageable pageable);

    String deleteUser(Long userId);
}
