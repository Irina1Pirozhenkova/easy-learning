package com.example.easy_learning.repository;

import com.example.easy_learning.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Set;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {

    Optional<Tutor> findByEmail(String email);
}

