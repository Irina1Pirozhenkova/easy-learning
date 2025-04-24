package com.example.easy_learning.repository;

import com.example.easy_learning.model.StudentsTasks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentsTasksRepository extends JpaRepository<StudentsTasks, Integer> {
   List<StudentsTasks> findByStudent_Id(Integer studentId);
}
