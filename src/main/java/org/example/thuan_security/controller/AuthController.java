package org.example.thuan_security.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtAuthenticationFilter;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.request.ResetPasswordRequest;
import org.example.thuan_security.service.factory.RegisterStrategy;
import org.example.thuan_security.repository.UserRepository;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RefreshTokenRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.*;
import org.example.thuan_security.service.*;
import org.example.thuan_security.service.factory.ResetPasswordStrategy;
import org.example.thuan_security.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.thuan_security.model.LogEnum.*;

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
    @Autowired
    private ResetPasswordStrategy resetPasswordStrategy;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RegisterFactory registerFactory;
    @Autowired
    private BlackListService blackListService;

    @PostMapping("/register")
    public UserKCLResponse register(@RequestBody RegisterRequest registerRequest) throws Exception {
        RegisterStrategy loginStrategy = registerFactory.getLoginStrategy();
        log.info(loginStrategy.toString());
        return loginStrategy.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<LoginResponse> validateOtp(@RequestParam String email, @RequestParam String otp, HttpServletRequest request) throws Exception {
        LoginResponse response = userService.validateLoginWithOtp(email, otp);
        if (response != null && String.valueOf(HttpStatus.OK.value()).equals(response.getCode())) {
            String ipAddress = convertTov4(request.getRemoteAddr());
            LocalDateTime localDateTime = LocalDateTime.now();
            logService.logActivity(email, String.valueOf(LOGIN), ipAddress, localDateTime);
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
    public ResponseEntity<ApiResponse> changeForgotPassword(@RequestParam String email, @RequestParam String token, @RequestParam String newPassword, HttpServletRequest request) throws Exception {
        ApiResponse response = userService.changeForgotPassword(email, token, newPassword);
        String ipAddress = convertTov4(request.getRemoteAddr());
        LocalDateTime localDateTime = LocalDateTime.now();
        logService.logActivity(email, String.valueOf(RESET_PASSWORD), ipAddress, localDateTime);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestParam String token, HttpServletRequest request) throws Exception {
        String email = jwtTokenProvider.extractClaims(token);
        blackList.addTokenToBlacklist(token);
        refreshTokenService.deleteRefreshToken(email);
        String ipAddress = convertTov4(request.getRemoteAddr());
        LocalDateTime localDateTime = LocalDateTime.now();
        logService.logActivity(email, String.valueOf(LOG_OUT), ipAddress, localDateTime);
        return ResponseEntity.ok(new ApiResponse<>(200, "Logged out successfully", 1, null));
    }

    public static String convertTov4(String ipAddress) {
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
            return ipAddress;
        }
        return ipAddress;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request,HttpServletRequest httpServletRequest) throws Exception {
        String refreshToken = request.getRefreshToken();
        String token=jwtAuthenticationFilter.getTokenFromRequest(httpServletRequest);
        if (!refreshTokenService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }

        String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
        log.info(email);
        Users user = userRepository.findByEmail(email);

        if (user != null) {
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream().map(SimpleGrantedAuthority::new).toList();
            String newAccessToken = jwtTokenProvider.createToken(new UsernamePasswordAuthenticationToken(email, null, authorities), email);
            blackListService.addTokenToBlacklist(token);
            return ResponseEntity.ok(new TokenResponse(newAccessToken));
        }
        return ResponseEntity.status(401).body("Invalid or expired refresh token");

    }
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable String id,@RequestBody ResetPasswordRequest resetPasswordRequest)  {
        return resetPasswordStrategy.resetPassword(id,resetPasswordRequest);
    }
}

