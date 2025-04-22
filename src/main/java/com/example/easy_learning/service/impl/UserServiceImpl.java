package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.User;
import com.example.easy_learning.repository.UserRepository;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Сохраняет пользователя с захешированным паролем.
     */
    @Override
    @Transactional
    public User create(User user) {
        // Хешируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Ищет пользователя по email.
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}