package com.example.easy_learning.repository;

import com.example.easy_learning.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {}
