package com.example.easy_learning.repository;

import com.example.easy_learning.model.Homework;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeworkRepository extends JpaRepository<Homework, Integer> {}
