package com.example.easy_learning.dto.auth;

import lombok.Data;

@Data
public class RefreshRequest {
  private String refreshToken;
}
