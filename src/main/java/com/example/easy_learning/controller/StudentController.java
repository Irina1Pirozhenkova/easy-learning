package com.example.easy_learning.controller;

import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.mapper.StudentMapper;
import com.example.easy_learning.mapper.StudentsHomeworkMapper;
import com.example.easy_learning.mapper.StudentsTutorsMapper;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.security.StudentJwtEntity;
import com.example.easy_learning.security.TutorJwtEntity;
import com.example.easy_learning.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
  public ResponseEntity<?> createStudent(@RequestBody StudentNRDto studentNRDto) {
    // Проверка авторизации
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неавторизован.");
    }

    // Проверяем, что это тьютор
    Object principal = authentication.getPrincipal();
    boolean isTutor = principal instanceof TutorJwtEntity;

    if (!isTutor) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Только репетиторы могут создавать студентов.");
    }

    // Основная логика создания
    Student toCreate = studentMapper.toNREntity(studentNRDto);
    StudentNRDto created = studentMapper.toNRDto(studentService.createStudent(toCreate));

    return ResponseEntity
            .status(HttpStatus.CREATED) // 201 Created
            .body(created);
  }


  @GetMapping("/{id}")
  public ResponseEntity<?> getStudent(@PathVariable Integer id,
                                      @RequestParam(required = false, defaultValue = "false") boolean full) {
    // достаём текущего студента из токена
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неавторизован.");//401
    }

    StudentJwtEntity studentJwt = (StudentJwtEntity) authentication.getPrincipal();
    Integer currentUserId = studentJwt.getId();

    // если студент хочет запросить не свои данные — запрет
    if (!currentUserId.equals(id)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещён.");//403
    }

    // всё ок — возвращаем данные
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
  public ResponseEntity<?> getAllStudents() {
    // Получаем пользователя из токена
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неавторизован.");
    }

    // Проверяем, кто именно залогинен
    Object principal = authentication.getPrincipal();
    boolean isTutor = principal instanceof TutorJwtEntity;

    if (!isTutor) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Только репетиторы могут просматривать всех студентов.");
    }

    // Всё ок — возвращаем список студентов
    List<Student> students = studentService.getAllStudents();
    List<StudentNRDto> studentNRDtos = studentMapper.toNRDtos(students)
            .stream().map(s -> {
              s.setPassword(null);
              return s;
            }).collect(Collectors.toList());

    return ResponseEntity.ok(studentNRDtos);
  }


  @PutMapping("/{id}")
  public ResponseEntity<?> updateStudent(@PathVariable Integer id, @RequestBody StudentRDto studentRDto) {
    // Проверка на авторизацию
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неавторизован.");
    }

    // Проверяем, что это тьютор
    Object principal = authentication.getPrincipal();
    boolean isTutor = principal instanceof TutorJwtEntity;

    if (!isTutor) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Только репетиторы могут обновлять данные студентов.");
    }

    // Основная логика обновления
    Student toUpdate = studentMapper.toNREntity(studentMapper.toNRDto(studentRDto));
    if (studentRDto.getHomeworks() != null) {
      toUpdate.setHomeworks(studentsHomeworkMapper.toEntitiesFromHDto(studentRDto.getHomeworks()));
    }
    if (studentRDto.getTutors() != null) {
      toUpdate.setTutors(studentsTutorsMapper.toEntitiesFromTDto(studentRDto.getTutors()));
    }

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
}
