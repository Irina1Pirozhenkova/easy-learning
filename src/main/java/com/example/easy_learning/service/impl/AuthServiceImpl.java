package com.example.easy_learning.service.impl;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.auth.JwtResponse;
import com.example.easy_learning.model.User;
import com.example.easy_learning.security.JwtTokenProvider;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authManager;
    private final UserService userService;
    private final JwtTokenProvider jwtProvider;

    @Override
    public JwtResponse login(JwtRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = userService.findByEmail(req.getUsername()).orElseThrow();
        return JwtResponse.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(jwtProvider.createAccessToken(auth))
                .refreshToken(jwtProvider.createRefreshToken(auth))
                .build();
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        if (!jwtProvider.validate(refreshToken))
            throw new RuntimeException("Invalid refresh token");
        Authentication auth = jwtProvider.getAuthentication(refreshToken);
        String access = jwtProvider.createAccessToken(auth);
        String refresh = jwtProvider.createRefreshToken(auth);
        return JwtResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }
}
