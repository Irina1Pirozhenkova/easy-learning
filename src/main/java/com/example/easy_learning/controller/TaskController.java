package com.example.easy_learning.controller;

import com.example.easy_learning.model.Task;
import com.example.easy_learning.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  /**
   * Создание новой задачи без файла.
   */
  @PostMapping(consumes = "application/json")
  public ResponseEntity<Task> createTask(@RequestBody Task task) {
    Task created = taskService.createTask(task);
    return ResponseEntity.ok(created);
  }

  /**
   * Создание новой задачи с загрузкой файла.
   * JSON-часть запроса должна быть передана в поле "task",
   * файл – в поле "file".
   */
  @PostMapping(value = "/with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Task> createTaskWithFile(@RequestPart("task") Task task,
                                                 @RequestPart("file") MultipartFile file) throws IOException {
    Task created = taskService.createTaskWithFile(task, file);
    return ResponseEntity.ok(created);
  }

  /**
   * Получение задачи по id.
   * Если параметр full=true, возвращаются все связи (используется метод getTaskByIdWithAllRelations).
   */
  @GetMapping("/{id}")
  public ResponseEntity<Task> getTask(@PathVariable Integer id,
                                      @RequestParam(value = "full", defaultValue = "false") boolean full) {
    Task task = full ? taskService.getTaskByIdWithAllRelations(id) : taskService.getTaskById(id);
    return ResponseEntity.ok(task);
  }

  /**
   * Получение списка всех задач.
   */
  @GetMapping
  public ResponseEntity<List<Task>> getAllTasks() {
    List<Task> tasks = taskService.getAllTasks();
    return ResponseEntity.ok(tasks);
  }

  /**
   * Обновление задачи. Если передан новый файл, он будет обработан.
   * JSON-часть запроса передаётся в поле "task", файл – в поле "file" (необязательный).
   */
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Task> updateTask(@PathVariable Integer id,
                                         @RequestPart("task") Task task,
                                         @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
    Task updated = taskService.updateTask(id, task, file);
    return ResponseEntity.ok(updated);
  }

  /**
   * Удаление задачи по id.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Возвращает файл фотографии для задачи по её ID.
   */
  @GetMapping("/{id}/photo")
  public ResponseEntity<byte[]> getTaskPhoto(@PathVariable Integer id) throws IOException {
    byte[] photo = taskService.getTaskPhoto(id);
    Task task = taskService.getTaskById(id);
    String photoPath = task.getPhotoUrl();

    // Определяем MIME-тип на основе расширения файла
    String contentType = "application/octet-stream";
    int dotIndex = photoPath.lastIndexOf('.');
    if (dotIndex != -1) {
      String ext = photoPath.substring(dotIndex + 1).toLowerCase();
      if ("png".equals(ext)) {
        contentType = "image/png";
      } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
        contentType = "image/jpeg";
      } else if ("gif".equals(ext)) {
        contentType = "image/gif";
      }
    }

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(photo);
  }
}
