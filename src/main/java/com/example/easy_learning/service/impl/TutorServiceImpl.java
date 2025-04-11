package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.repository.TutorRepository;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Tutor create(Tutor tutor) {
        tutor.setPassword(passwordEncoder.encode(tutor.getPassword()));
        return tutorRepository.save(tutor);
    }

    @Override
    @Transactional
    public Tutor update(Integer id, Tutor updatedTutor) {
        Tutor existing = getById(id);

        existing.setPersonalInfo(updatedTutor.getPersonalInfo());

        if (!existing.getPassword().equals(updatedTutor.getPassword())) {
            existing.setPassword(passwordEncoder.encode(updatedTutor.getPassword()));
        }

        if (updatedTutor.getTasks() != null) {
            existing.setTasks(updatedTutor.getTasks());
        }

        if (updatedTutor.getHomeworks() != null) {
            existing.setHomeworks(updatedTutor.getHomeworks());
        }

        if (updatedTutor.getStudents() != null) {
            existing.setStudents(updatedTutor.getStudents());
        }

        return tutorRepository.save(existing);
    }

    @Override
    public Tutor getById(Integer id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor not found with id: " + id));
    }

    @Override
    public Tutor getByEmail(String email) {
        return tutorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Тьютор с email " + email + " не найден"));
    }


    @Override
    public Tutor getByIdWithRelations(Integer id) {
        // Пока нет кастомного запроса с @EntityGraph — возвращаем обычный getById
        return getById(id);
    }

    @Override
    public Set<Tutor> getAll() {
        return Set.copyOf(tutorRepository.findAll());
    }
}
