package com.example.easy_learning.service;

import com.example.easy_learning.model.Student;

import java.util.List;

public interface StudentService {
  Student createStudent(Student student);
  Student getStudentById(Integer id);
  Student getStudentByIdWithAllRelations(Integer id);
  Student updateStudent(Integer id, Student updatedStudent);
  void deleteStudent(Integer id);
  List<Student> getAllStudents();
  Student getByEmail(String email);
}
