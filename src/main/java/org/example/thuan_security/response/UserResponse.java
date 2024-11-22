package org.example.thuan_security.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    @NotBlank(message = "Full name cannot be null or empty")
    String fullName;
    String email;
    String role;
    String imgUrl;
}
