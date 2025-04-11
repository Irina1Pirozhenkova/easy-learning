package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Student;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.StudentRepository;
import com.example.easy_learning.service.StudentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Student createStudent(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        return studentRepository.save(student);
    }

    @Override
    public Student getStudentById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
    }

    @Override
    public Student getStudentByIdWithAllRelations(Integer id) {
        return studentRepository.findStudentWithAssociationsById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
    }

    @Override
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

    @Override
    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Студент с email " + email + " не найден"));
    }
}
