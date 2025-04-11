package com.example.easy_learning.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

  private String email;
  private String password;
  private String userType;
}
