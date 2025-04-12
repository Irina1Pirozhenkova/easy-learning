package com.example.easy_learning.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Login request payload")
public class JwtRequest {

    @Schema(description = "Email", example = "user@mail.com")
    @NotBlank(message = "Username must not be blank")//400
    private String username;

    @Schema(description = "Password", example = "12345")
    @NotBlank(message = "Password must not be blank")//400
    private String password;
}
