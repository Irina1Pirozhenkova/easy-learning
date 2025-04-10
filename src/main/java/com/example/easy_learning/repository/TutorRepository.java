package com.example.easy_learning.repository;

import com.example.easy_learning.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {

    Optional<Tutor> findByPersonalInfoEmail(String email);

}

