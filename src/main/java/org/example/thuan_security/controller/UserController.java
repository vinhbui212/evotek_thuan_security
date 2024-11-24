package org.example.thuan_security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtAuthenticationFilter;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.UserResponse;
import org.example.thuan_security.service.UserActivityLogService;
import org.example.thuan_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.thuan_security.controller.AuthController.convertTov4;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserActivityLogService logService;
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getUserInfo(HttpServletRequest request) {
        String token = jwtAuthenticationFilter.getTokenFromRequest(request);

        if (token == null) {
            throw new RuntimeException("Missing or invalid Authorization token");
        }

        UserResponse response = userService.getUserInfo(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/changePassword")
    public ApiResponse changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            String token = jwtAuthenticationFilter.getTokenFromRequest(request);
            String email = jwtTokenProvider.extractClaims(token);
            String ipAddress=convertTov4(request.getRemoteAddr());
            LocalDateTime localDateTime=LocalDateTime.now();
            logService.logActivity(email,"CHANGE_PASSWORD",ipAddress,localDateTime);
            return userService.changePassword(changePasswordRequest, email);

        } catch (Exception e) {
            return new ApiResponse<>(401,"Wrong current password",0, List.of(""));
        }
    }
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateUserInfo(HttpServletRequest request,
                                                      @Valid @RequestBody UserResponse userResponse) {
        String token=jwtAuthenticationFilter.getTokenFromRequest(request);
        String email=jwtTokenProvider.extractClaims(token);
        String ipAddress=convertTov4(request.getRemoteAddr());
        LocalDateTime localDateTime=LocalDateTime.now();
        logService.logActivity(email,"UPDATE_INFOR",ipAddress,localDateTime);
        ApiResponse response = userService.updateUserInfo(token, userResponse);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "admin" ;
    }
}
