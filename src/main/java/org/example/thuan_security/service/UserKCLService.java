package org.example.thuan_security.service;

import org.example.thuan_security.request.LoginRequest;
import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.request.RegisterRequestKCL;
import org.example.thuan_security.response.LoginResponse;
import org.example.thuan_security.response.UserKCLResponse;

public interface UserKCLService {
    UserKCLResponse register(RegisterRequest request);

}
