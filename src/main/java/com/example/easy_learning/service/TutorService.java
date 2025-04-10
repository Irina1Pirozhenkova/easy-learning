package com.example.easy_learning.service;

import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.repository.TutorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TutorService {

  private final TutorRepository tutorRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public Tutor createTutor(Tutor tutor) {
    tutor.setPassword(passwordEncoder.encode(tutor.getPassword()));
    return tutorRepository.save(tutor);
  }

  public Tutor getTutorById(Integer id) {
    return tutorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tutor not found with ID = " + id));
  }

  public Tutor updateTutor(Integer id, Tutor updatedTutor) {
    Tutor existingTutor = getTutorById(id);
    // При изменении пароля – хэшируем заново
    if (!existingTutor.getPassword().equals(updatedTutor.getPassword())) {
      existingTutor.setPassword(passwordEncoder.encode(updatedTutor.getPassword()));
    }
    existingTutor.setPersonalInfo(updatedTutor.getPersonalInfo());
    // при необходимости обновляем другие поля
    return tutorRepository.save(existingTutor);
  }

  public void deleteTutor(Integer id) {
    tutorRepository.deleteById(id);
  }

  public List<Tutor> getAllTutors() {
    return tutorRepository.findAll();
  }

  public Tutor getByEmail(String email) {
    return tutorRepository.findByPersonalInfoEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Преподаватель с email " + email + " не найден"));
  }

}
