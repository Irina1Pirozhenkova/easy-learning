package com.example.easy_learning.controller;

import com.example.easy_learning.model.Student;
import com.example.easy_learning.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @PostMapping
  public ResponseEntity<Student> createStudent(@RequestBody Student student) {
    Student created = studentService.createStudent(student);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Student> getStudent(@PathVariable Integer id,
                                            @RequestParam(required = false, defaultValue = "false") boolean full) {
    Student student = full
            ? studentService.getStudentByIdWithAllRelations(id)
            : studentService.getStudentById(id);
    return ResponseEntity.ok(student);
  }

  @GetMapping
  public ResponseEntity<List<Student>> getAllStudents() {
    List<Student> students = studentService.getAllStudents();
    return ResponseEntity.ok(students);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Student> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
    Student updated = studentService.updateStudent(id, student);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
    studentService.deleteStudent(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Добавляет новые домашние задания для студента.
   * Принимает набор id домашних заданий и возвращает обновленного студента.
   */
  @PostMapping("/{id}/homeworks")
  public ResponseEntity<?> addHomeworksToStudent(@PathVariable Integer id,
                                                 @RequestBody Set<Integer> homeworkIds) {
    try {
      Student updated = studentService.addHomeworksToStudent(id, homeworkIds);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * Удаляет связи с домашними заданиями у студента.
   * Принимает список id домашних заданий, которые необходимо убрать.
   */
  @DeleteMapping("/{id}/homeworks")
  public ResponseEntity<Student> removeHomeworksFromStudent(@PathVariable Integer id,
                                                            @RequestBody List<Integer> homeworkIds) {
    Student updated = studentService.removeHomeworksFromStudent(id, homeworkIds);
    return ResponseEntity.ok(updated);
  }
}
