package com.example.easy_learning.service;

import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.StudentsHomework;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
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

  public Student updateStudent(Integer id, Student updatedStudent) {
    Student existingStudent = getStudentById(id);

    if (!existingStudent.getPassword().equals(updatedStudent.getPassword())) {
      existingStudent.setPassword(passwordEncoder.encode(updatedStudent.getPassword()));
    }
    existingStudent.setStudentPersonalInfo(updatedStudent.getStudentPersonalInfo());
    return studentRepository.save(existingStudent);
  }

  public void deleteStudent(Integer id) {
    studentRepository.deleteById(id);
  }

  public List<Student> getAllStudents() {
    return studentRepository.findAll();
  }

  /**
   * Добавляет новые домашние задания (StudentsHomework) для студента.
   * Если хотя бы один из переданных id уже присутствует у студента,
   * метод бросает RuntimeException.
   *
   * @param studentId   идентификатор студента
   * @param homeworkIds набор id домашних заданий для добавления
   * @return обновленный объект Student с новыми связями
   */
  @Transactional
  public Student addHomeworksToStudent(Integer studentId, Set<Integer> homeworkIds) {
    // Получаем студента со всеми связями (используя метод с @EntityGraph)
    Student student = studentRepository.findStudentWithAssociationsById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + studentId));

    // Проверяем, что ни один из новых id не пересекается с уже существующими
    for (Integer hwId : homeworkIds) {
      boolean exists = student.getHomeworks().stream()
              .anyMatch(sh -> sh.getHomework().getId().equals(hwId));
      if (exists) {
        throw new RuntimeException("Homework with id " + hwId + " already exists for the student");
      }
    }

    // Для каждого нового id получаем Homework и создаем связь StudentsHomework
    for (Integer hwId : homeworkIds) {
      Homework homework = homeworkRepository.findById(hwId)
              .orElseThrow(() -> new RuntimeException("Homework with id " + hwId + " not found"));
      StudentsHomework sh = new StudentsHomework();
      sh.setHomework(homework);
      sh.setStudent(student);
    }

    return studentRepository.save(student);
  }

  /**
   * Удаляет из студента связи с домашними заданиями, идентификаторы которых переданы в homeworkIds.
   * Для каждой найденной связи вызывается setter с null для student, что при orphanRemoval
   * приводит к удалению соответствующей записи из БД.
   *
   * @param studentId   идентификатор студента
   * @param homeworkIds список id домашних заданий, связи с которыми необходимо убрать
   * @return обновленный объект Student без указанных связей
   */
  @Transactional
  public Student removeHomeworksFromStudent(Integer studentId, List<Integer> homeworkIds) {
    // Получаем студента со всеми связями
    Student student = studentRepository.findStudentWithAssociationsById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + studentId));

    // Удаляем связи, если id домашек содержатся в homeworkIds.
    // Используем removeIf, и для каждой связи вызываем setter с null для student.
    student.getHomeworks().removeIf(sh -> {
      if (homeworkIds.contains(sh.getHomework().getId())) {
        sh.setStudent(null);
        return true;
      }
      return false;
    });

    return studentRepository.save(student);
  }
}
