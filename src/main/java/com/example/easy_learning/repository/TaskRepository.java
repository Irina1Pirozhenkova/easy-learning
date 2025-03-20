package com.example.easy_learning.repository;

import com.example.easy_learning.model.Task;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer> {

  @EntityGraph(attributePaths = {"homeworks"})
  @Query("SELECT t FROM Task t WHERE t.id = :id")
  Optional<Task> findByIdWithAllRelations(@Param("id") Integer id);
}
