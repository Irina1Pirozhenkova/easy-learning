package com.example.easy_learning.controller;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.mapper.TaskMapper;
import com.example.easy_learning.model.ClassLevel;
import com.example.easy_learning.model.Subject;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.model.User;
import com.example.easy_learning.service.TaskService;
import com.example.easy_learning.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/frontend")
public class ViewController {

  private final UserService userService;
  private final TaskService taskService;
  private final TaskMapper taskMapper;

  @PreAuthorize("hasRole('TUTOR')")
  @GetMapping("/new")
  public String newTaskForm(Model model) {
    model.addAttribute("taskForm", new TaskNRDto());
    // передаём списки enum'ов
    model.addAttribute("classLevels", ClassLevel.values());
    model.addAttribute("subjects", Subject.values());
    return "task_new";
  }

  @PreAuthorize("hasRole('TUTOR')")
  @PostMapping("/new")
  public String createTask(
          @ModelAttribute TaskNRDto taskForm,
          @RequestParam("file") MultipartFile file,
          Authentication auth
  ) throws IOException {
    // Получаем репетитора
    String email = auth.getName();
    User tutor = userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Tutor not found"));
    // Заполняем DTO
    taskForm.setTutorId(tutor.getId());
    // Создаём задачу с файлом
    taskService.createTaskWithFile(
            taskMapper.toNREntity(taskForm),
            file
    );
    return "redirect:/frontend/tasks";
  }

  @PreAuthorize("hasRole('TUTOR')")
  @GetMapping("/tasks")
  public String tasksPage(
          @RequestParam(value = "sort", required = false) String sort,
          Model model,
          Authentication auth
  ) {
    // 1) Получили email залогиненного пользователя
    String email = auth.getName();

    // 2) Загрузили из БД объект User
    User tutor = userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Tutor not found: " + email));

    // 3) Собрали задачи в список
    List<Task> tasks = new ArrayList<>(tutor.getTasks());

    // 4) Отсортировали, если передан параметр sort
    if ("className".equals(sort)) {
      tasks.sort(Comparator.comparing(Task::getClassName));
    } else if ("subject".equals(sort)) {
      tasks.sort(Comparator.comparing(Task::getSubject));
    } else if ("topic".equals(sort)) {
      tasks.sort(Comparator.comparing(Task::getTopic));
    } else if ("difficulty".equals(sort)) {
      tasks.sort(Comparator.comparing(Task::getDifficulty));
    } else if ("description".equals(sort)) {
      tasks.sort(Comparator.comparing(Task::getDescription));
    } else {
      // дефолтная сортировка: по классу, затем по предмету
      tasks.sort(Comparator
              .comparing(Task::getClassName)
              .thenComparing(Task::getSubject)
      );
    }

    // 5) Сгруппировали: Map<класс, Map<предмет, List<Task>>>
    Map<String, Map<String, List<Task>>> grouped = tasks.stream()
            .collect(Collectors.groupingBy(
                    t -> t.getClassName().name(),
                    Collectors.groupingBy(t -> t.getSubject().name())
            ));

    model.addAttribute("groupedTasks", grouped);
    model.addAttribute("sort", sort);

    return "tasks";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";   // src/main/resources/templates/login.html
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register"; // templates/register.html
  }

  @GetMapping
  public String home() {
    return "index";   // templates/index.html
  }
}

