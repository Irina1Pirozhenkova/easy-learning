package com.example.easy_learning.service.impl;

import com.example.easy_learning.dto.ProfileDto;
import com.example.easy_learning.model.PersonalInfo;
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

  @Override
  public Optional<User> findById(Integer id) {
    return userRepository.findById(id);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }


  @Override
  @Transactional
  public User updateProfile(ProfileDto dto) {
    User user = userRepository.findById(dto.getId())
            .orElseThrow(() -> new RuntimeException("User not found: " + dto.getId()));

    // email
    user.setEmail(dto.getEmail());

    // пароль, если указали
    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    // личная информация
    var pi = user.getPersonalInfo();
    if (pi == null) {
      pi = new PersonalInfo();
      user.setPersonalInfo(pi);
    }
    pi.setFirstname(dto.getFirstname());
    pi.setLastname(dto.getLastname());
    pi.setBirthdate(dto.getBirthdate());
    pi.setPhone(dto.getPhone());
    pi.setTelegram(dto.getTelegram());

    return userRepository.save(user);
  }
}