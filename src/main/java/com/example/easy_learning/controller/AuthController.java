package com.example.easy_learning.controller;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.RegisterResponse;
import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.JwtResponse;
import com.example.easy_learning.mapper.StudentMapper;
import com.example.easy_learning.mapper.TutorMapper;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.StudentService;
import com.example.easy_learning.service.TutorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ResourceBundle;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final StudentMapper studentMapper;
    private final TutorMapper tutorMapper;
    private final StudentService studentService;
    private final TutorService tutorService;

    @PostMapping("/login")
    public JwtResponse login(@RequestBody @Validated JwtRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register/{user-type}")
    public ResponseEntity<?> register(@PathVariable("user-type") String userType, @RequestBody RegisterDto registerDto) {
        if ("student".equals(userType)) {
            Student student = studentMapper.toEntity(registerDto);
            Student created = studentService.createStudent(student);
            return ResponseEntity.ok(RegisterResponse.builder().email(created.getEmail()).password(created.getPassword()).userType(userType).build());
        }
        if ("tutor".equals(userType)) {
            Tutor tutor = tutorMapper.toTutor(registerDto);
            Tutor created = tutorService.create(tutor);
            return ResponseEntity.ok(RegisterResponse.builder().email(created.getEmail()).password(created.getPassword()).userType(userType).build());
        }
        return ResponseEntity.badRequest().body("Нельзя создать пользователя с таким user type");
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestBody String refreshToken) {
        return authService.refresh(refreshToken);
    }
}
