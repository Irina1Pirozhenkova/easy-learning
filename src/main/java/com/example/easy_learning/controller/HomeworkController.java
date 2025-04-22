package com.example.easy_learning.controller;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.mapper.*;
import com.example.easy_learning.model.Homework;
import com.example.easy_learning.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/homeworks")
@RequiredArgsConstructor
public class HomeworkController {

//  private final HomeworkService homeworkService;
//  private final HomeworkMapper homeworkMapper;
//  private final TutorMapper tutorMapper;
//  private final HomeworkTaskMapper homeworkTaskMapper;
//  private final StudentsHomeworkMapper studentsHomeworkMapper;
//
//  @PostMapping
//  public ResponseEntity<Homework> createHomework(@RequestBody HomeworkRDto homeworkRDto) {
//    Homework toCreate = homeworkMapper.toNREntity(homeworkMapper.toNRDto(homeworkRDto));
//    toCreate.setTutor(tutorMapper.toNREntity(homeworkRDto.getTutor()));
//    toCreate.setTasks(homeworkTaskMapper.toEntitiesFromTDtos(homeworkRDto.getTasks()));
//    toCreate.setStudents(studentsHomeworkMapper.toEntitiesFromSDto(homeworkRDto.getStudents()));
//
//    Homework created = homeworkService.createHomework(toCreate);
//    return ResponseEntity.ok(created);
//  }
//
//  @GetMapping("/{id}")
//  public ResponseEntity<?> getHomework(@PathVariable Integer id,
//                                              @RequestParam(value = "full", defaultValue = "false") boolean full) {
//    if (full) {
//      Homework homework = homeworkService.getHomeworkWithAssociationsById(id);
//      HomeworkRDto toResponse = homeworkMapper.toRDto(homeworkMapper.toNRDto(homework));
//      toResponse.setTasks(homeworkTaskMapper.toTDtos(homework.getTasks()));
//      toResponse.setStudents(studentsHomeworkMapper.toSDtoSet(homework.getStudents()));
//      toResponse.setTutor(tutorMapper.toNRDto(homework.getTutor()));
//      return ResponseEntity.ok(toResponse);
//    }
//    else {
//      HomeworkNRDto toResponse = homeworkMapper.toNRDto(homeworkService.getHomeworkById(id));
//      return ResponseEntity.ok(toResponse);
//    }
//  }
//
//  @GetMapping
//  public ResponseEntity<Set<HomeworkNRDto>> getAllHomeworks() {
//    List<Homework> homeworks = homeworkService.getAllHomeworks();
//    Set<HomeworkNRDto> homeworkNRDtos = homeworkMapper.toNRDtos(Set.copyOf(homeworks));
//    return ResponseEntity.ok(homeworkNRDtos);
//  }
//
//  @PutMapping("/{id}")
//  public ResponseEntity<HomeworkRDto> updateHomework(@PathVariable Integer id, @RequestBody HomeworkRDto homeworkRDto) {
//    Homework toUpdate = homeworkMapper.toNREntity(homeworkMapper.toNRDto(homeworkRDto));
//    if (homeworkRDto.getTutor() != null) toUpdate.setTutor(tutorMapper.toNREntity(homeworkRDto.getTutor()));
//    if (homeworkRDto.getTasks() != null) toUpdate.setTasks(homeworkTaskMapper.toEntitiesFromTDtos(homeworkRDto.getTasks()));
//    if (homeworkRDto.getStudents() != null) toUpdate.setStudents(studentsHomeworkMapper.toEntitiesFromSDto(homeworkRDto.getStudents()));
//
//    toUpdate = homeworkService.updateHomework(id, toUpdate);
//
//    homeworkRDto = homeworkMapper.toRDto(homeworkMapper.toNRDto(toUpdate));
//    homeworkRDto.setTasks(homeworkTaskMapper.toTDtos(toUpdate.getTasks()));
//    homeworkRDto.setStudents(studentsHomeworkMapper.toSDtoSet(toUpdate.getStudents()));
//    homeworkRDto.setTutor(tutorMapper.toNRDto(toUpdate.getTutor()));
//    return ResponseEntity.ok(homeworkRDto);
//  }
//
//  @DeleteMapping("/{id}")
//  public ResponseEntity<Void> deleteHomework(@PathVariable Integer id) {
//    homeworkService.deleteHomework(id);
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Добавляет новые задачи (HomeworkTask) в домашнее задание.
//   * Принимает набор id задач в теле запроса.
//   */
//  @PostMapping("/{id}/tasks")
//  public ResponseEntity<?> addTasksToHomework(@PathVariable Integer id,
//                                                     @RequestBody Set<Integer> taskIds) {
//    try {
//      Homework updated = homeworkService.addTasksToHomework(id, taskIds);
//      return ResponseEntity.ok(updated);
//    }
//    catch (Exception e) {
//      return ResponseEntity.badRequest().body(e.getMessage());
//    }
//  }
//
//  /**
//   * Удаляет связи с задачами из домашнего задания.
//   * Принимает список id задач, связи с которыми необходимо удалить.
//   */
//  @DeleteMapping("/{id}/tasks")
//  public ResponseEntity<Homework> removeTasksFromHomework(@PathVariable Integer id,
//                                                          @RequestBody List<Integer> taskIds) {
//    Homework updated = homeworkService.removeTasksFromHomework(id, taskIds);
//    return ResponseEntity.ok(updated);
//  }
}
