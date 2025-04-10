package com.example.easy_learning.security;

import com.example.easy_learning.model.Student;

import java.util.Collections;

public final class StudentJwtEntityFactory {

    public static StudentJwtEntity create(Student student) {
        return new StudentJwtEntity(
                student.getId(),
                student.getStudentPersonalInfo().getEmail(),
                student.getPassword()
        );
    }
}
