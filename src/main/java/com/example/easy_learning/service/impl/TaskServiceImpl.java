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
    private final String uploadDir = "./uploads";

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
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String uniqueName = UUID.randomUUID().toString() + ext;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Path path = dir.resolve(uniqueName);
        file.transferTo(path.toFile());
        return uploadDir + File.separator + uniqueName;
    }

    @Override
    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

}
