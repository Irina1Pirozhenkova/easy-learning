package com.example.easy_learning.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response containing JWT tokens")
@Builder
public class JwtResponse {
    private Integer id;
    private String username;
    private String accessToken;
    private String refreshToken;
}
