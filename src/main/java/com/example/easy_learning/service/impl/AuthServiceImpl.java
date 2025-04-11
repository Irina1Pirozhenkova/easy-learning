package com.example.easy_learning.service.impl;

import com.example.easy_learning.dto.JwtResponse;
import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.security.JwtTokenProvider;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.StudentService;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final StudentService studentService;
    private final TutorService tutorService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        // Аутентификация
        var auth = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()
                )
        );

        // Попробуем сначала как студент
        try {
            Student student = studentService.getByEmail(loginRequest.getUsername());
            return buildStudentTokens(student);
        } catch (RuntimeException e) {
            // Если студент не найден — пробуем репетитора
            Tutor tutor = tutorService.getByEmail(loginRequest.getUsername());
            return buildTutorTokens(tutor);
        }
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        String userType = jwtTokenProvider.getUserType(refreshToken);
        if ("student".equals(userType)) {
            return jwtTokenProvider.refreshStudentTokens(refreshToken);
        } else if ("tutor".equals(userType)) {
            return jwtTokenProvider.refreshTutorTokens(refreshToken);
        } else {
            throw new RuntimeException("Invalid user type");
        }
    }

    private JwtResponse buildStudentTokens(Student student) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(student.getId().longValue());
        jwtResponse.setUsername(student.getEmail());
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        return jwtResponse;
    }

    private JwtResponse buildTutorTokens(Tutor tutor) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(tutor.getId().longValue());
        jwtResponse.setUsername(tutor.getEmail());
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        return jwtResponse;
    }
}
