package org.example.thuan_security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtAuthenticationFilter;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.request.SearchRequest;
import org.example.thuan_security.request.UserSearchRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.UserResponse;
import org.example.thuan_security.service.UserActivityLogService;
import org.example.thuan_security.service.factory.DeleteStraegy;
import org.example.thuan_security.service.user.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.thuan_security.controller.AuthController.convertTov4;

@RestController
@RequestMapping("/api/user")
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
    @Autowired
    private DeleteStraegy deleteStraegy;

    @GetMapping("/info")
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
        String ipAddress=convertTov4(request.getRemoteAddr());
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        LocalDateTime localDateTime=LocalDateTime.now();
        logService.logActivity(email,"UPDATE_INFOR",ipAddress,localDateTime);
        ApiResponse response = userService.updateUserInfo(token, userResponse);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasPermission('user','CREATE')")
    public String admin() {
        return "admin";
    }


    @PostMapping()
    public String createUser(@RequestBody RegisterRequest request) {

        return userService.createUser(request);
    }

    @PreAuthorize("hasPermission('user','UPDATE')")
    @PutMapping("/{id}/update")
    public String lock(
            @PathVariable String id,
            @RequestBody RegisterRequest registerRequest) {

        return deleteStraegy.deleteStraegy(id,registerRequest);

        }

    @PreAuthorize("hasPermission('user','READ')")
    @GetMapping("/all")
    public Page<UserResponse> getAllUsers(@ParameterObject SearchRequest searchRequest) {
        return userService.getAllUsers(searchRequest);
    }

    @PreAuthorize("hasPermission('user','DELETE')")
    @DeleteMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @GetMapping("/search")
    public List<UserResponse> searchUsers(
            @ParameterObject UserSearchRequest searchRequest) {

        return userService.searchUsers(searchRequest);
    }
}
