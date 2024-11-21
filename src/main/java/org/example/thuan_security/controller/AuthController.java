package org.example.thuan_security.controller;

import lombok.RequiredArgsConstructor;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * API Login
     */
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
//        LoginResponse response = userService.login(loginRequest);
//
//        if ("200".equals(response.getCode())) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//        }
//    }

    /**
     * API Register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest) {
        ApiResponse response = userService.register(registerRequest);

        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<LoginResponse> validateOtp(@RequestParam String email, @RequestParam String otp) {
        LoginResponse response = userService.validateLoginWithOtp(email, otp);
        if ("200".equals(response.getCode())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    @GetMapping("/verifiedAccount")
    public String verifiedAccount(@RequestParam String email) {
        userService.isVerifiedAccount(email);
        return "verified";
    }

    @PostMapping("/sendMailForgotPassword")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        ApiResponse response = userService.sendMailForgotPassword(email);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/changeForgotPassword")
    public ResponseEntity<ApiResponse> changeForgotPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword) {
        ApiResponse response = userService.changeForgotPassword(email, otp, newPassword);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

