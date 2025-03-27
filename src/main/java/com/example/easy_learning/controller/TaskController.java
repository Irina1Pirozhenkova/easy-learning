package com.example.easy_learning.controller;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.dto.TaskRDto;
import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.mapper.HomeworkTaskMapper;
import com.example.easy_learning.mapper.TaskMapper;
import com.example.easy_learning.mapper.TutorMapper;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;
  private final TutorMapper tutorMapper;
  private final HomeworkTaskMapper homeworkTaskMapper;

  /**
   * Создание новой задачи без файла.
   */
  @PostMapping(consumes = "application/json")
  public ResponseEntity<Task> createTask(@RequestBody TaskNRDto taskNRDto) {
    Task toCreate = taskMapper.toNREntity(taskNRDto);
    toCreate.setTutor(null);
    Task created = taskService.createTask(toCreate);
    return ResponseEntity.ok(created);
  }

  /**
   * Создание новой задачи с загрузкой файла.
   * JSON-часть запроса должна быть передана в поле "task",
   * файл – в поле "file".
   */
  @PostMapping(value = "/with-file")
  public ResponseEntity<Task> createTaskWithFile(@RequestPart("task") TaskNRDto taskNRDto,
                                                 @RequestPart("file") MultipartFile file) throws IOException {
    Task toCreate = taskMapper.toNREntity(taskNRDto);
    toCreate.setTutor(null);
    Task created = taskService.createTaskWithFile(toCreate, file);
    return ResponseEntity.ok(created);
  }

  /**
   * Получение задачи по id.
   * Если параметр full=true, возвращаются все связи (используется метод getTaskByIdWithAllRelations).
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getTask(@PathVariable Integer id,
                                      @RequestParam(value = "full", defaultValue = "false") boolean full) {
    Task task;
    if (full) {
      task = taskService.getTaskByIdWithAllRelations(id);
      TaskRDto taskRDto = taskMapper.toRDto(taskMapper.toNRDto(task));
      TutorNRDto tutorNRDto = tutorMapper.toNRDto(task.getTutor());
      taskRDto.setTutor(tutorMapper.toNRDto(task.getTutor()));
      taskRDto.setHomeworks(homeworkTaskMapper.toHDtos(task.getHomeworks()));
      return ResponseEntity.ok(taskRDto);
    }
    else {
      return ResponseEntity.ok(taskMapper.toNRDto(taskService.getTaskById(id)));
    }
  }

  /**
   * Получение списка всех задач.
   */
  @GetMapping
  public ResponseEntity<List<TaskNRDto>> getAllTasks() {
    List<Task> tasks = taskService.getAllTasks();
    List<TaskNRDto> taskNRDtos = taskMapper.toNRDtos(tasks);
    return ResponseEntity.ok(taskNRDtos);
  }

  /**
   * Обновление задачи. Если передан новый файл, он будет обработан.
   * JSON-часть запроса передаётся в поле "task", файл – в поле "file" (необязательный).
   */
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TaskRDto> updateTask(@PathVariable Integer id,
                                         @RequestPart("task") TaskNRDto taskNRDto,
                                         @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
    Task toUpdate = taskMapper.toNREntity(taskNRDto);
    Task updated = taskService.updateTask(id, toUpdate, file);
    TaskRDto taskRDto = taskMapper.toRDto(taskMapper.toNRDto(updated));
    taskRDto.setHomeworks(homeworkTaskMapper.toHDtos(updated.getHomeworks()));
    taskRDto.setTutor(tutorMapper.toNRDto(updated.getTutor()));
    return ResponseEntity.ok(taskRDto);
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
