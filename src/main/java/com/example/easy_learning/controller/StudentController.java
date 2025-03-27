package com.example.easy_learning.controller;

import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.mapper.StudentMapper;
import com.example.easy_learning.mapper.StudentsHomeworkMapper;
import com.example.easy_learning.mapper.StudentsTutorsMapper;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;
  private final StudentMapper studentMapper;
  private final StudentsHomeworkMapper studentsHomeworkMapper;
  private final StudentsTutorsMapper studentsTutorsMapper;

  @PostMapping
  public ResponseEntity<StudentNRDto> createStudent(@RequestBody StudentNRDto studentNRDto) {
    Student toCreate = studentMapper.toNREntity(studentNRDto);
    StudentNRDto created = studentMapper.toNRDto(studentService.createStudent(toCreate));
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getStudent(@PathVariable Integer id,
                                      @RequestParam(required = false, defaultValue = "false") boolean full) {
    if (full) {
      Student student = studentService.getStudentById(id);
      StudentRDto studentRDto = studentMapper.toRDto(studentMapper.toNRDto(student));
      studentRDto.setHomeworks(studentsHomeworkMapper.toHDtoSet(student.getHomeworks()));
      studentRDto.setTutors(studentsTutorsMapper.toTDtoSet(student.getTutors()));
      studentRDto.setPassword(null);
      return ResponseEntity.ok(studentRDto);
    }
    StudentNRDto studentNRDto = studentMapper.toNRDto(studentService.getStudentById(id));
    studentNRDto.setPassword(null);
    return ResponseEntity.ok(studentNRDto);
  }

  @GetMapping
  public ResponseEntity<List<StudentNRDto>> getAllStudents() {
    List<Student> students = studentService.getAllStudents();
    List<StudentNRDto> studentNRDtos = studentMapper.toNRDtos(students)
            .stream().map(s -> {
              s.setPassword(null);
              return s;
            }).collect(Collectors.toList());
    return ResponseEntity.ok(studentNRDtos);
  }

  @PutMapping("/{id}")
  public ResponseEntity<StudentRDto> updateStudent(@PathVariable Integer id, @RequestBody StudentRDto studentRDto) {
    Student toUpdate = studentMapper.toNREntity(studentMapper.toNRDto(studentRDto));
    toUpdate.setHomeworks(studentsHomeworkMapper.toEntitiesFromHDto(studentRDto.getHomeworks()));
    toUpdate.setTutors(studentsTutorsMapper.toEntitiesFromTDto(studentRDto.getTutors()));

    Student updated = studentService.updateStudent(id, toUpdate);

    studentRDto = studentMapper.toRDto(studentMapper.toNRDto(updated));
    studentRDto.setTutors(studentsTutorsMapper.toTDtoSet(updated.getTutors()));
    studentRDto.setHomeworks(studentsHomeworkMapper.toHDtoSet(updated.getHomeworks()));
    return ResponseEntity.ok(studentRDto);
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
