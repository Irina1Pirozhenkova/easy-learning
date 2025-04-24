package com.example.easy_learning.controller;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.auth.JwtResponse;
import com.example.easy_learning.model.Role;
import com.example.easy_learning.model.User;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.UserService;
import com.example.easy_learning.service.props.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final UserService userService;
  private final JwtProperties props;

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(
          @RequestBody @Validated JwtRequest req,
          HttpServletResponse response
  ) {
    JwtResponse jwt = authService.login(req);

    ResponseCookie cookie = ResponseCookie.from("accessToken", jwt.getAccessToken())
            .httpOnly(true)
            .path("/")
            .maxAge(props.getAccess() * 3600)
            .build();
    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(jwt);
  }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody RegisterDto dto) {
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(dto.getPassword());
    if (dto.isTutor()) {
      user.getRoles().add(Role.TUTOR);
    }
    else {
      user.getRoles().add(Role.STUDENT);
    }
    return ResponseEntity.ok(userService.create(user));
  }
}