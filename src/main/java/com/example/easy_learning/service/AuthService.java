package com.example.easy_learning.service;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.auth.JwtResponse;
import org.springframework.stereotype.Service;


@Service
public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(String refreshToken);
}
