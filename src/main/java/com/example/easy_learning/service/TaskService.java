package com.example.easy_learning.service;

import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.TaskRepository;
import jakarta.transaction.Transactional;
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
@Transactional
public class TaskService {

  private final TaskRepository taskRepository;

  // Путь к папке, где храним все загруженные файлы
  private final String uploadDir = "./uploads";

  /**
   * Создаём новую задачу (Task) без файла.
   */
  public Task createTask(Task task) {
    return taskRepository.save(task);
  }

  /**
   * Создаём новую задачу (Task) с загрузкой файла.
   * Генерируем уникальное имя файла, сохраняем в папку uploads,
   * в поле photoUrl записываем путь к файлу.
   */
  public Task createTaskWithFile(Task task, MultipartFile file) throws IOException {
    // Сохраняем файл на диск и получаем URL
    String photoUrl = saveFile(file);
    task.setPhotoUrl(photoUrl);

    return taskRepository.save(task);
  }

  /**
   * Получить Task по ID.
   */
  public Task getTaskById(Integer id) {
    return taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found with ID = " + id));
  }

  public Task getTaskByIdWithAllRelations(Integer id) {
    Task task = taskRepository.findByIdWithAllRelations(id)
            .orElseThrow(() -> new RuntimeException("Task not found with ID = " + id));
    return task;
  }

  /**
   * Обновить Task, в том числе, если нужен перезагрузить новый файл.
   */
  public Task updateTask(Integer id, Task updatedTask, MultipartFile file) throws IOException {
    Task existingTask = getTaskByIdWithAllRelations(id);

    // Обновляем поля
    existingTask.setClassName(updatedTask.getClassName());
    existingTask.setSubject(updatedTask.getSubject());
    existingTask.setTopic(updatedTask.getTopic());
    existingTask.setDifficulty(updatedTask.getDifficulty());
    existingTask.setTutor(updatedTask.getTutor()); // при необходимости

    // Если передаётся новый файл, перезапишем
    if (file != null && !file.isEmpty()) {
      String photoUrl = saveFile(file);
      existingTask.setPhotoUrl(photoUrl);
    }
    taskRepository.save(existingTask);
    return existingTask;
  }

  /**
   * Удаление Task.
   */
  public void deleteTask(Integer id) {
    taskRepository.deleteById(id);
  }

  /**
   * Получить все Task.
   */
  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }

  /**
   * Логика сохранения файла в папку uploads.
   */
  private String saveFile(MultipartFile file) throws IOException {
    // Генерируем уникальное имя файла
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    String uniqueFileName = UUID.randomUUID().toString() + extension;

    // Создаём директорию uploads, если её нет
    Path uploadPath = Paths.get("/app", uploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    // Полный путь к файлу
    Path filePath = uploadPath.resolve(uniqueFileName);

    // Сохраняем файл на диск
    if (!Files.exists(filePath)) {
      Files.createFile(filePath);
    }
    file.transferTo(filePath.toFile());

    // Здесь вы можете вернуть абсолютный путь, или URL, или относительный путь
    // Например, вернём просто "uploads/имя_файла"
    return uploadDir + File.separator + uniqueFileName;
  }

  /**
   * Получает содержимое файла photo для Task по его ID.
   * Если файл не найден, выбрасывается RuntimeException.
   *
   * @param taskId идентификатор задачи
   * @return массив байт с содержимым файла
   * @throws IOException если не удалось прочитать файл
   */
  public byte[] getTaskPhoto(Integer taskId) throws IOException {
    Task task = getTaskById(taskId);
    String photoPath = task.getPhotoUrl();
    Path path = Paths.get(photoPath);
//    if (!Files.exists(path)) {
//      throw new RuntimeException("Photo file not found for task with ID = " + taskId);
//    }
    return Files.readAllBytes(path);
  }
}
