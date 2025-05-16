package com.example.easy_learning.service;

import com.example.easy_learning.dto.ProfileDto;
import com.example.easy_learning.model.User;
import java.util.Optional;

public interface UserService {
    // Создать нового пользователя
    User create(User user);

    // Найти пользователя по email
    Optional<User> findByEmail(String email);
    Optional<User> findById(Integer id);

    User updateProfile(ProfileDto dto);
    boolean existsByEmail(String email);
}