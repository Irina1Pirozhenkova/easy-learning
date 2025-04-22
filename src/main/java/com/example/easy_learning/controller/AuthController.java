package com.example.easy_learning.controller;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.auth.JwtResponse;
import com.example.easy_learning.model.Role;
import com.example.easy_learning.model.User;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final UserService userService;

  @PostMapping("/login")
  public JwtResponse login(@RequestBody @Validated JwtRequest req) {
    return authService.login(req);
  }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody RegisterDto dto) {
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(dto.getPassword());
    if (dto.isTutor()) {
      user.getRoles().add(Role.TUTOR);
    }
    user.getRoles().add(Role.STUDENT);
    return ResponseEntity.ok(userService.create(user));
  }
}