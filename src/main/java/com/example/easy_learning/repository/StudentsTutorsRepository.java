package com.example.easy_learning.repository;

import com.example.easy_learning.model.StudentsTutors;
import com.example.easy_learning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentsTutorsRepository extends JpaRepository<StudentsTutors, Integer> {
    boolean existsByTutorAndStudent(User tutor, User student);

}
