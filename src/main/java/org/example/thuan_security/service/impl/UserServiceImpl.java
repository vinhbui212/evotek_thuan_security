package org.example.thuan_security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.exception.UserException;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.RoleRepository;
import org.example.thuan_security.repository.UserRepository;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.RegisterResponseDTO;
import org.example.thuan_security.response.UserResponse;
import org.example.thuan_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
                log.info("User {} logged in successfully", email);
                String token = jwtTokenProvider.createToken(authentication, email);
                log.info(token);
                LocalDateTime expiration = jwtTokenProvider.extractExpiration(token);
                log.info(expiration.toString());
//                String role = jwtTokenProvider.extractRole(token);
//                log.info(role);
                return new LoginResponse("200", "Login success", token,expiration);
            }
        } catch (Exception e) {
            return new LoginResponse("401", "Wrong email or password", "",null);
        }

        return new LoginResponse("401", "Wrong email or password", "",null);
    }


    @Override
    public ApiResponse register(RegisterRequest registerRequest) {
        if (isPasswordValid(registerRequest)) {
            ApiResponse<String> apiResponse = new ApiResponse<>();
            apiResponse.setCode(0);
            apiResponse.setMessage("Password and confirm password do not match");
            apiResponse.setStatus(400);
            apiResponse.setData(List.of(""));
            return apiResponse;
        }

        if (isEmailValid(registerRequest)) {
            Users newUser = new Users();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            Roles role = roleRepository.findByName("ROLE_USER");
            Set<Roles> roles = new HashSet<>();
            roles.add(role);
            newUser.setRoles(roles);
            newUser.setFullName(registerRequest.getFullName());
            userRepository.save(newUser);

            RegisterResponseDTO responseDTO = new RegisterResponseDTO(
                    newUser.getEmail(),
                    newUser.getFullName(),
                    ""
            );
            ApiResponse<RegisterResponseDTO> apiResponse = new ApiResponse<>();
            apiResponse.setCode(1);
            apiResponse.setMessage("Register success");
            apiResponse.setStatus(200);
            apiResponse.setData(List.of(responseDTO));
            return apiResponse;
        } else {
            ApiResponse<String> apiResponse = new ApiResponse<>();
            apiResponse.setCode(0);
            apiResponse.setMessage("Email already exists");
            apiResponse.setStatus(409);
            apiResponse.setData(Collections.singletonList(""));

            return apiResponse;
        }
    }

//    public UserResponse getUserInfo(String token) {
//        if (token == null || !token.startsWith("Bearer ")) {
//            throw new IllegalArgumentException("Invalid token");
//        }
//
//        try {
//            // Loại bỏ tiền tố "Bearer " để lấy JWT
//
//            // Lấy thông tin từ claims
//            String email = claims.getSubject(); // Trích xuất email (hoặc username)
//            String role = (String) claims.get("role"); // Trích xuất role từ claims (nếu có)
//
//            // Tạo UserResponse
//            UserResponse userResponse = new UserResponse();
//            userResponse.setEmail(email);
//            userResponse.setRole(role);
//
//            return userResponse;
//
//        } catch (Exception e) {
//            throw new IllegalStateException("Invalid or expired token", e);
//        }
//    }


    public boolean isPasswordValid(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return true;
        }
        return false;
    }

    public boolean isEmailValid(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()) == null) {
            return true;
        }
        return false;
    }
}
