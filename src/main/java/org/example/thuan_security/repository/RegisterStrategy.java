package org.example.thuan_security.repository;

import org.example.thuan_security.request.RegisterRequest;
import org.example.thuan_security.response.ApiResponse1;
import org.example.thuan_security.response.UserKCLResponse;

public interface RegisterStrategy {
    UserKCLResponse register(RegisterRequest registerRequest);
}
