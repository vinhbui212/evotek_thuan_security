package org.example.thuan_security.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.config.JwtTokenProvider;
import org.example.thuan_security.controller.FileUploadController;
import org.example.thuan_security.model.Roles;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.RoleRepository;
import org.example.thuan_security.repository.UserRepository;
import org.example.thuan_security.request.ChangePasswordRequest;
import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.*;
import org.example.thuan_security.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RefreshTokenService refreshTokenService;


    private final Map<String, String> tokenStorage = new HashMap<>();
    private final Cache<String, String> otpCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Autowired
    private FileUploadController fileUploadController;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Users user = userRepository.findByEmail(loginRequest.getEmail());
            if (user == null) {
                return new LoginResponse("404", "User not found", "",null,null);
            }

            if (!user.isVerified()) {
                return new LoginResponse("403", "Account not verified. Please verify your account.", "",null,null);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
                log.info("User {} logged in successfully. Sending OTP...", email);


                String otp = generateOTP();
                otpCache.put(email, otp);
                otpCache.asMap().forEach((key, value) -> {
                    System.out.println("Key: " + key + ", Value: " + value);
                });
                emailService.sendMail(email, "Your OTP Code", "Your OTP code is: " + otp);

                return new LoginResponse("202", "OTP sent to email. Please verify.", null,null,null);
            }
        } catch (Exception e) {
            return new LoginResponse("401", "Wrong email or password", "",null,null);
        }

        return new LoginResponse("401", "Wrong email or password", "",null,null);
    }
    @Override
    public LoginResponse validateLoginWithOtp(String email, String otp) {
        if (validateOtp(email,otp)) {
            deleteOtp(email);
            Users user = userRepository.findByEmail(email);
            if (user != null) {
                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                String token = jwtTokenProvider.createToken(
                        new UsernamePasswordAuthenticationToken(email, null, authorities),
                        email);

                String rftoken=refreshTokenService.createRefreshToken(email);

                LocalDateTime expiration = jwtTokenProvider.extractExpiration(token);

                return new LoginResponse("200", "Login successful", token,rftoken, expiration);
            } else {
                return new LoginResponse("404", "User not found", "",null,null);
            }
        } else {
            return new LoginResponse("401", "Invalid or expired OTP", "", null,null);
        }
    }



    @Override
    public UserKCLResponse register(RegisterRequest registerRequest) {
//        if (isPasswordValid(registerRequest)) {
//            ApiResponse1<String> apiResponse = new ApiResponse1<>();
//            apiResponse.setMessage("Password and confirm password do not match");
//            apiResponse.setResult("");
//            return apiResponse;
//        }

        if (isEmailValid(registerRequest)) {


            Users newUser = new Users();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            Roles roles = roleRepository.findByName("ROLE_USER");
            String roleName = roles.getName();
            newUser.setRoles(Collections.singleton(roleName));
            String fullname=registerRequest.getFirstName()+ " "+ registerRequest.getLastName();
            newUser.setFullName(fullname);
            userRepository.save(newUser);
            String verificationLink = "http://localhost:8080/api/auth/verifiedAccount?email=" + registerRequest.getEmail();
            emailService.sendMail(
                    registerRequest.getEmail(),
                    "Xác nhận đăng ký",
                    "<p>Chào " + fullname + ",</p>"
                            + "<p>Vui lòng nhấn vào liên kết bên dưới để xác nhận đăng ký tài khoản:</p>"
                            + "<a href=\"" + verificationLink + "\">Xác nhận đăng ký</a>"
            );

            RegisterResponseDTO responseDTO = new RegisterResponseDTO(
                    newUser.getEmail(),
                    newUser.getFullName(),
                    ""
            );
            UserKCLResponse apiResponse = new UserKCLResponse();
           apiResponse.setUsername(registerRequest.getUsername());
            return apiResponse;
        } else {
            UserKCLResponse apiResponse = new UserKCLResponse();
            apiResponse.setUsername(registerRequest.getUsername());


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
            userResponse.setImgUrl(user.getImage_url());

            return userResponse;

        }

    @Override
    public ApiResponse updateUserInfo(String token, UserResponse userResponse) {
        String email = jwtTokenProvider.extractClaims(token);
        try {
            Users users = userRepository.findByEmail(email);
            users.setFullName(userResponse.getFullName());
            userRepository.save(users);
            ApiResponse apiResponse = new ApiResponse<>(200, "Updated", 1, null);
            return apiResponse;
        } catch (Exception e) {
                return new ApiResponse<>(404, "Empty name", 0, null);

        }
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

        if (user != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String token = jwtTokenProvider.createToken(authentication, email);

            tokenStorage.put(email, token);


            String subject = "Reset Your Password";
            String resetLink = "http://localhost:8080/api/users/reset-password?token=" + token;
            String message = "Dear " + user.getFullName() + ",\n\n" +
                    "We received a request to reset your password. Please click the link below to reset it:\n" +
                    resetLink + "\n\n"
                    +
                    "This link will expire in 30 minutes.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Best regards,\nYour Team";


            emailService.sendMail(email, subject, message);

            log.info("Reset password link sent to email: {}", email);

            return new ApiResponse<>(200, "Link to reset your password has been sent to your email.", 1, List.of(""));
        }

        return new ApiResponse<>(401, "Cannot find user with this email", 0, List.of(""));
    }


    @Override
    public ApiResponse changeForgotPassword(String email, String token, String newPassword) {
        if (isValidOTP(email, token)) {
            Users user = userRepository.findByEmail(email);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            emailService.sendMail(email,"Mật khẩu thay đổi","Mật khẩu của bạn đã được thay đổi ");

            return new ApiResponse<>(200, "Password changed successfully", 1, List.of(""));

        }
        return new ApiResponse<>(401,"Wrong OTP",0,List.of(""));
    }

    @Override
    public String createUser(RegisterRequest registerRequest) {
        Users newUser = new Users();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        Roles roles = roleRepository.findByName("ROLE_USER");
        String roleName = roles.getName();
        newUser.setRoles(Collections.singleton(roleName));
        String fullname=registerRequest.getFirstName()+ " "+ registerRequest.getLastName();
        newUser.setFullName(fullname);
        newUser.setDob(registerRequest.getDob());
        newUser.setVerified(true);
        userRepository.save(newUser);

        return "Created user successfull";
    }


    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public String generateRefreshToken(String token){
        return UUID.randomUUID().toString();
    }

    public boolean validateOtp(String key, String inputOtp) {
        String cachedOtp = otpCache.getIfPresent(key);
        return cachedOtp != null && cachedOtp.equals(inputOtp);
    }

    // Xóa OTP sau khi dùng hoặc không còn hiệu lực
    public void deleteOtp(String key) {
        otpCache.invalidate(key);
    }


    public boolean isValidOTP(String email, String otp) {
        if (tokenStorage.containsKey(email) && tokenStorage.get(email).equals(otp)) {
            tokenStorage.remove(email);
            return true;
        }
        return false;
    }

//    public boolean isPasswordValid(RegisterRequest registerRequest) {
//        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
//            return true;
//        }
//        return false;
//    }

    public boolean isEmailValid(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()) == null) {
            return true;
        }
        return false;
    }
}
