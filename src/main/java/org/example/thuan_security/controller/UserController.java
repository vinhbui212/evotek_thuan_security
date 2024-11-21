package org.example.thuan_security.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.thuan_security.config.JwtAuthenticationFilter;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.UserResponse;
import org.example.thuan_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
            return userService.changePassword(changePasswordRequest, email);
        } catch (Exception e) {
            return new ApiResponse<>(401,"Wrong current password",0, List.of(""));
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "admin" ;
    }
}
