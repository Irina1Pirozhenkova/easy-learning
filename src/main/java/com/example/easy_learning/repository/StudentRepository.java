package com.example.easy_learning.repository;

import com.example.easy_learning.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {}
