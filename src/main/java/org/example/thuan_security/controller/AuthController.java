package org.example.thuan_security.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.UserRepository;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RefreshTokenRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.TokenResponse;
import org.example.thuan_security.service.BlackListService;
import org.example.thuan_security.service.RefreshTokenService;
import org.example.thuan_security.service.UserActivityLogService;
import org.example.thuan_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BlackListService blackList;
    @Autowired
    private UserActivityLogService logService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest,HttpServletRequest request) throws Exception {
        ApiResponse response = userService.register(registerRequest);

        if (response.getStatus() == 200) {
            String ipAddress=convertTov4(request.getRemoteAddr());
            LocalDateTime localDateTime=LocalDateTime.now();
            logService.logActivity(registerRequest.getEmail(),"REGISTER",ipAddress,localDateTime);
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
    public ResponseEntity<LoginResponse> validateOtp(@RequestParam String email, @RequestParam String otp, HttpServletRequest request) throws Exception {
        LoginResponse response = userService.validateLoginWithOtp(email, otp);
        if ("200".equals(response.getCode())) {
            String ipAddress=convertTov4(request.getRemoteAddr());
            LocalDateTime localDateTime=LocalDateTime.now();
            logService.logActivity(email,"LOGIN",ipAddress,localDateTime);
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
    public ResponseEntity<ApiResponse> changeForgotPassword(@RequestParam String email, @RequestParam String token, @RequestParam String newPassword,HttpServletRequest request) throws Exception {
        ApiResponse response = userService.changeForgotPassword(email, token, newPassword);
        String ipAddress=convertTov4(request.getRemoteAddr());
        LocalDateTime localDateTime=LocalDateTime.now();
        logService.logActivity(email,"RESET_PASSWORD",ipAddress,localDateTime);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestParam String token,HttpServletRequest request) throws Exception {
        String email= jwtTokenProvider.extractClaims(token);
        blackList.addTokenToBlacklist(token);
        String ipAddress=convertTov4(request.getRemoteAddr());
        LocalDateTime localDateTime=LocalDateTime.now();
        logService.logActivity(email,"LOG_OUT",ipAddress,localDateTime);
        return ResponseEntity.ok(new ApiResponse<>(200, "Logged out successfully", 1, null));
    }
    public static String convertTov4(String ipAddress){
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
            return ipAddress;
        }
        return ipAddress;
    }
//    @PostMapping("/refresh-token")
//    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
//        String refreshToken = request.getRefreshToken();
//
//        if (!refreshTokenService.isRefreshTokenValid(refreshToken)) {
//            return ResponseEntity.status(401).body("Invalid or expired refresh token");
//        }
//
//        String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
//        log.info(email);
//        Users users= userRepository.findByEmail(email);
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String newAccessToken = jwtTokenProvider.createToken(authentication,email);
//
//        return ResponseEntity.ok(new TokenResponse(newAccessToken));
//    }
}

