package org.example.thuan_security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.RoleRepository;
import org.example.thuan_security.repository.UserRepository;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.RegisterResponseDTO;
import org.example.thuan_security.response.UserResponse;
import org.example.thuan_security.service.EmailService;
import org.example.thuan_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private EmailService emailService;
    private final Map<String, String> otpLoginStorage = new HashMap<>();
    private final Map<String, String> otpStorage = new HashMap<>();

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Users user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                return new LoginResponse("404", "User not found", "", null);
            }

            if (!user.isVerified()) {
                return new LoginResponse("403", "Account not verified. Please verify your account.", "", null);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
                log.info("User {} logged in successfully. Sending OTP...", email);


                String otp = generateOTP();
                otpLoginStorage.put(email, otp);
                emailService.sendMail(email, "Your OTP Code", "Your OTP code is: " + otp);

                return new LoginResponse("202", "OTP sent to email. Please verify.", null, null);
            }
        } catch (Exception e) {
            return new LoginResponse("401", "Wrong email or password", "", null);
        }

        return new LoginResponse("401", "Wrong email or password", "", null);
    }
    @Override
    public LoginResponse validateLoginWithOtp(String email, String otp) {
        if (otpLoginStorage.containsKey(email) && otpLoginStorage.get(email).equals(otp)) {
            otpLoginStorage.remove(email);

            Users user = userRepository.findByEmail(email);
            if (user != null) {
                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                String token = jwtTokenProvider.createToken(
                        new UsernamePasswordAuthenticationToken(email, null, authorities),
                        email
                );
                LocalDateTime expiration = jwtTokenProvider.extractExpiration(token);

                return new LoginResponse("200", "Login successful", token, expiration);
            } else {
                return new LoginResponse("404", "User not found", "", null);
            }
        } else {
            return new LoginResponse("401", "Invalid or expired OTP", "", null);
        }
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
            Roles roles = roleRepository.findByName("ROLE_USER");
            String roleName = roles.getName();
            newUser.setRoles(Collections.singleton(roleName));
            newUser.setFullName(registerRequest.getFullName());
            userRepository.save(newUser);
            String verificationLink = "http://localhost:8080/api/auth/verifiedAccount?email=" + registerRequest.getEmail();
            emailService.sendMail(
                    registerRequest.getEmail(),
                    "Xác nhận đăng ký",
                    "<p>Chào " + registerRequest.getFullName() + ",</p>"
                            + "<p>Vui lòng nhấn vào liên kết bên dưới để xác nhận đăng ký tài khoản:</p>"
                            + "<a href=\"" + verificationLink + "\">Xác nhận đăng ký</a>"
            );

            RegisterResponseDTO responseDTO = new RegisterResponseDTO(
                    newUser.getEmail(),
                    newUser.getFullName(),
                    ""
            );
            ApiResponse<RegisterResponseDTO> apiResponse = new ApiResponse<>();
            apiResponse.setCode(1);
            apiResponse.setMessage("Register success. Please check your email for verification.");
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

    @Override
    public UserResponse getUserInfo(String token) {
            String email = jwtTokenProvider.extractClaims(token);
            String role = jwtTokenProvider.extractRole(token);
            Users user = userRepository.findByEmail(email);

            // Tạo UserResponse
            UserResponse userResponse = new UserResponse();
            userResponse.setEmail(email);
            userResponse.setRole(role);
            userResponse.setFullName(user.getFullName());
            userResponse.setPassword(user.getPassword());

            return userResponse;

        }

    @Override
    public UserResponse updateUserInfo(String token, UserResponse userResponse) {
        return null;
    }

    @Override
    public boolean isVerifiedAccount(String email){
        Users user = userRepository.findByEmail(email);
        user.setVerified(true);
        userRepository.save(user);
        return true;
    }

    @Override
    public ApiResponse changePassword(ChangePasswordRequest changePasswordRequest, String email) {
        Users user = userRepository.findByEmail(email);
        if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);
            emailService.sendMail(email,"Mật khẩu thay đổi","Mật khẩu của bạn đã được thay đổi ");
            ApiResponse<String> apiResponse = new ApiResponse<>();
            apiResponse.setCode(1);
            apiResponse.setMessage("Password changed successfully");
            apiResponse.setStatus(200);
            apiResponse.setData(List.of(""));
            return apiResponse;
        }
        else {
            return new ApiResponse<>(401,"Wrong current password",0,List.of(""));
        }

    }

    @Override
    public ApiResponse sendMailForgotPassword(String email) {
        Users user = userRepository.findByEmail(email);
        if(user != null){

            String otp = generateOTP();
            otpStorage.put(email, otp);

            String subject = "Reset Your Password";
            String message = "Your OTP for resetting password is: " + otp + "\nThis OTP will expire in 5 minutes.";

            emailService.sendMail(email, subject, message);

            log.info("OTP sent to email: {}", email);

            return new ApiResponse<>(200, "Link to reset your pass is sent to email.", 1, List.of(""));

        }
        return new ApiResponse<>(401,"Cant not found email",0,List.of(""));

    }

    @Override
    public ApiResponse changeForgotPassword(String email, String otp, String newPassword) {
        if (isValidOTP(email, otp)) {
            Users user = userRepository.findByEmail(email);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            emailService.sendMail(email,"Mật khẩu thay đổi","Mật khẩu của bạn đã được thay đổi ");

            return new ApiResponse<>(200, "Password changed successfully", 1, List.of(""));

        }
        return new ApiResponse<>(401,"Wrong OTP",0,List.of(""));
    }

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }


    public boolean isValidOTP(String email, String otp) {
        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(otp)) {
            otpStorage.remove(email);
            return true;
        }
        return false;
    }

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
