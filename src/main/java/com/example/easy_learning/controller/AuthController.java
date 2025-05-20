package com.example.easy_learning.controller;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.auth.JwtResponse;
import com.example.easy_learning.dto.auth.RefreshRequest;
import com.example.easy_learning.exception.UserExistsException;
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
    JwtResponse jwt = authService.login(req); //проверяет учётные данные и выдаёт токены
    ResponseCookie cookie = ResponseCookie.from("accessToken", jwt.getAccessToken())
           //кладём accessToken в HTTPonly cookie accessToken(браузер автоматическиотсылал,JavaScriptкнемунеимелдоступа
            .httpOnly(true)
            .path("/") // cookie действует на всё приложение
            .maxAge(props.getAccess() * 3600) // время жизни в секундах
            .build();
    return ResponseEntity.ok() //В теле ответа возвращаем тот же JwtResponse
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

    if (userService.existsByEmail(user.getEmail())) {
      throw new UserExistsException("Пользователь с данным email уже существует");
    }
    return ResponseEntity.ok(userService.create(user));
  }
  @PostMapping("/refresh")
  public JwtResponse refresh(@RequestBody RefreshRequest refReq) {
    return authService.refresh(refReq.getRefreshToken());
  }
}