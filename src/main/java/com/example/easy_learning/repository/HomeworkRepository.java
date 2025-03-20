package com.example.easy_learning.repository;

import com.example.easy_learning.model.Homework;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HomeworkRepository extends JpaRepository<Homework, Integer> {

    @Query("SELECT h FROM Homework h WHERE h.id = :id")
    @EntityGraph(attributePaths = {
            "tutor",
            "students",
            "students.student",
            "tasks",
            "tasks.task"
    })
    Optional<Homework> findHomeworkWithAssociationsById(@Param("id") Integer id);
}
