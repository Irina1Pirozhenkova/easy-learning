package com.example.easy_learning.controller;

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

  private final HomeworkService homeworkService;

  @PostMapping
  public ResponseEntity<Homework> createHomework(@RequestBody Homework homework) {
    Homework created = homeworkService.createHomework(homework);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Homework> getHomework(@PathVariable Integer id,
                                              @RequestParam(value = "full", defaultValue = "false") boolean full) {
    Homework homework = full
            ? homeworkService.getHomeworkWithAssociationsById(id)
            : homeworkService.getHomeworkById(id);
    return ResponseEntity.ok(homework);
  }

  @GetMapping
  public ResponseEntity<List<Homework>> getAllHomeworks() {
    List<Homework> homeworks = homeworkService.getAllHomeworks();
    return ResponseEntity.ok(homeworks);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Homework> updateHomework(@PathVariable Integer id, @RequestBody Homework homework) {
    Homework updated = homeworkService.updateHomework(id, homework);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteHomework(@PathVariable Integer id) {
    homeworkService.deleteHomework(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Добавляет новые задачи (HomeworkTask) в домашнее задание.
   * Принимает набор id задач в теле запроса.
   */
  @PostMapping("/{id}/tasks")
  public ResponseEntity<?> addTasksToHomework(@PathVariable Integer id,
                                                     @RequestBody Set<Integer> taskIds) {
    try {
      Homework updated = homeworkService.addTasksToHomework(id, taskIds);
      return ResponseEntity.ok(updated);
    }
    catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * Удаляет связи с задачами из домашнего задания.
   * Принимает список id задач, связи с которыми необходимо удалить.
   */
  @DeleteMapping("/{id}/tasks")
  public ResponseEntity<Homework> removeTasksFromHomework(@PathVariable Integer id,
                                                          @RequestBody List<Integer> taskIds) {
    Homework updated = homeworkService.removeTasksFromHomework(id, taskIds);
    return ResponseEntity.ok(updated);
  }
}
