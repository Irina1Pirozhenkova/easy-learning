package com.example.easy_learning.repository;

import com.example.easy_learning.model.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

  @Query("SELECT s FROM Student s WHERE s.id = :id")
  @EntityGraph(attributePaths = {
          "homeworks",
          "homeworks.homework",
          "homeworks.homework.tasks",
          "tutors",
          "tutors.tutor"
  })
  Optional<Student> findStudentWithAssociationsById(@Param("id") Integer id);
}
