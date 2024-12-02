package org.example.thuan_security.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {
    private String client_id;
    private String client_secret;
    private String refresh_token;
}
