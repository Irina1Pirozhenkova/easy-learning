package com.example.easy_learning.service;

import com.example.easy_learning.model.*;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

  private final StudentRepository studentRepository;
  private final HomeworkRepository homeworkRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public Student createStudent(Student student) {
    student.setPassword(passwordEncoder.encode(student.getPassword()));
    return studentRepository.save(student);
  }

  public Student getStudentById(Integer id) {
    return studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
  }

  public Student getStudentByIdWithAllRelations(Integer id) {
    return studentRepository.findStudentWithAssociationsById(id)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
  }

  @Transactional
  public Student updateStudent(Integer id, Student updatedStudent) {
    Student existingStudent = getStudentByIdWithAllRelations(id);
    if (updatedStudent.getHomeworks() != null) {
      existingStudent.setHomeworks(updatedStudent.getHomeworks());
    }

    if (updatedStudent.getTutors() != null) {
      existingStudent.setTutors(updatedStudent.getTutors());
    }

    if (!existingStudent.getPassword().equals(updatedStudent.getPassword())) {
      existingStudent.setPassword(passwordEncoder.encode(updatedStudent.getPassword()));
    }
    if (updatedStudent.getStudentPersonalInfo() != null) {
      existingStudent.setStudentPersonalInfo(updatedStudent.getStudentPersonalInfo());
    }
    return studentRepository.save(existingStudent);
  }

  public void deleteStudent(Integer id) {
    studentRepository.deleteById(id);
  }

  public List<Student> getAllStudents() {
    return studentRepository.findAll();
  }
}
