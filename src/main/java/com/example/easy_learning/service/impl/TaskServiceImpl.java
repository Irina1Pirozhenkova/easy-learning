package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.TaskRepository;
import com.example.easy_learning.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
//  private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
  private final Path uploadDir = Paths.get("/app/uploads");
  @Override
  public Task createTask(Task task) {
    return taskRepository.save(task);
  }
  @Override
  public Task createTaskWithFile(Task task, MultipartFile file) throws IOException {
    String photoUrl = saveFile(file);
    task.setPhotoUrl(photoUrl);
    return taskRepository.save(task);
  }
  @Override
  public Task getTaskByIdWithAllRelations(Integer id) {
    return taskRepository.findByIdWithAllRelations(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
  }
  @Override
  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }
  @Override
  public Task updateTask(Integer id, Task updatedTask, MultipartFile file) throws IOException {
    Task existingTask = getTaskByIdWithAllRelations(id);
    existingTask.setClassName(updatedTask.getClassName());
    existingTask.setSubject(updatedTask.getSubject());
    existingTask.setTopic(updatedTask.getTopic());
    existingTask.setDifficulty(updatedTask.getDifficulty());
    existingTask.setTutor(updatedTask.getTutor());
    if (file != null && !file.isEmpty()) {
      existingTask.setPhotoUrl(saveFile(file));
    }
    return taskRepository.save(existingTask);
  }
  @Override
  public byte[] getTaskPhoto(Integer taskId) throws IOException {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
    Path path = Paths.get(task.getPhotoUrl());
    return Files.readAllBytes(path);
  }
  @Override
  public void deleteTask(Integer id) {
    taskRepository.deleteById(id);
  }
  private String saveFile(MultipartFile file) throws IOException {
    // Получаем расширение
    String original = file.getOriginalFilename();
    String ext = original != null && original.contains(".")
            ? original.substring(original.lastIndexOf('.'))
            : "";
    String uniqueName = UUID.randomUUID() + ext;
    // Создаём директорию (если ещё нет)
    Files.createDirectories(uploadDir);
    // Абсолютный путь для сохранения
    Path filePath = uploadDir.resolve(uniqueName);
    // Сохраняем файл
    file.transferTo(filePath.toFile());
    // Возвращаем URL для доступа
    return "/uploads/" + uniqueName;
  }
  @Override
  public Task getTaskById(Integer id) {
    return taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
  }

}
