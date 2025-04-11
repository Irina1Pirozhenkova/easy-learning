package com.example.easy_learning.service;

import com.example.easy_learning.model.Task;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TaskService {
  Task createTask(Task task);
  Task createTaskWithFile(Task task, MultipartFile file) throws IOException;
  Task getTaskById(Integer id);
  Task getTaskByIdWithAllRelations(Integer id);
  Task updateTask(Integer id, Task updatedTask, MultipartFile file) throws IOException;
  void deleteTask(Integer id);
  List<Task> getAllTasks();
  byte[] getTaskPhoto(Integer taskId) throws IOException;
}
