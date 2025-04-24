package com.example.easy_learning.controller;

import com.example.easy_learning.dto.ProfileDto;
import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.mapper.TaskMapper;
import com.example.easy_learning.model.*;
import com.example.easy_learning.repository.StudentsTasksRepository;
import com.example.easy_learning.security.UserJwtEntity;
import com.example.easy_learning.service.StudentsTutorsService;
import com.example.easy_learning.service.TaskService;
import com.example.easy_learning.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
  private final StudentsTutorsService studentsTutorsService;
  private final StudentsTasksRepository studentsTasksRepo;

  // 2.1 Страница «Мои репетиторы»
  @PreAuthorize("hasRole('STUDENT')")
  @GetMapping("/tutors")
  public String tutorsPage(Model model, Authentication auth) {
    Integer studentId = ((UserJwtEntity) auth.getPrincipal()).getId();
    model.addAttribute("tutors", studentsTutorsService.getTutorsForStudent(studentId));
    return "tutors";
  }

  // 2.2 Просмотр одного репетитора
  @PreAuthorize("hasRole('STUDENT')")
  @GetMapping("/tutors/{id}")
  public String viewTutor(@PathVariable Integer id, Model model) {
    User tutor = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("Tutor not found: " + id));
    model.addAttribute("tutor", tutor);
    return "tutor_view";
  }

  @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
  @GetMapping("/profile")
  public String profileForm(Model model, Authentication auth) {
    // получаем текущего пользователя
    String email = auth.getName();
    User user = userService.findByEmail(email).orElseThrow();
    ProfileDto dto = new ProfileDto();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setFirstname(user.getPersonalInfo() != null ? user.getPersonalInfo().getFirstname() : null);
    dto.setLastname(user.getPersonalInfo() != null ? user.getPersonalInfo().getLastname() : null);
    dto.setBirthdate(user.getPersonalInfo() != null ? user.getPersonalInfo().getBirthdate() : null);
    dto.setPhone(user.getPersonalInfo() != null ? user.getPersonalInfo().getPhone() : null);
    dto.setTelegram(user.getPersonalInfo() != null ? user.getPersonalInfo().getTelegram() : null);

    model.addAttribute("profile", dto);
    return "profile";
  }

  @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
  @PostMapping("/profile")
  public String updateProfile(@ModelAttribute("profile") ProfileDto dto,
                              RedirectAttributes redirect) {
    userService.updateProfile(dto);
    redirect.addFlashAttribute("success", "Данные сохранены");
    return "redirect:/frontend/profile";
  }

  @PreAuthorize("hasRole('TUTOR')")
  @GetMapping("/students")
  public String studentsPage(Model model, Authentication auth) {
    // получаем id репетитора из аутентификации
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId();
    model.addAttribute("students", studentsTutorsService.getStudentsForTutor(tutorId));
    return "students";
  }

  @PreAuthorize("hasRole('TUTOR')")
  @PostMapping("/students")
  public String addStudent(@RequestParam("studentId") Integer studentId, Authentication auth) {
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId();
    studentsTutorsService.addStudentToTutor(tutorId, studentId);
    return "redirect:/frontend/students";
  }

  @GetMapping("/tasks/{id}")
  public String viewTask(@PathVariable Integer id, Model model) {
    Task task = taskService.getTaskById(id);
    model.addAttribute("task", task);
    return "task_view";
  }

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
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId();
    List<User> students = studentsTutorsService.getStudentsForTutor(tutorId);

    model.addAttribute("groupedTasks", grouped);
    model.addAttribute("sort", sort);
    model.addAttribute("studentsList", students);

    return "tasks";
  }

  @PostMapping("/tasks/assign")
  @PreAuthorize("hasRole('TUTOR')")
  public String assignTask(@RequestParam Integer taskId,
                           @RequestParam Integer studentId,
                           Authentication auth,
                           RedirectAttributes redirect) {
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId();
    studentsTutorsService.assignTaskToStudent(tutorId, taskId, studentId);
    redirect.addFlashAttribute("assignSuccess", "Задача назначена студенту");
    return "redirect:/frontend/tasks";
  }

  @GetMapping
  public String home(Model model, Authentication auth) {
    // если залогинен студент
    if (auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
      Integer studentId = ((UserJwtEntity) auth.getPrincipal()).getId();
      List<Task> myTasks = studentsTasksRepo
              .findByStudent_Id(studentId).stream()
              .map(StudentsTasks::getTask)
              .toList();
      model.addAttribute("myTasks", myTasks);
    }
    return "index";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";   // src/main/resources/templates/login.html
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register"; // templates/register.html
  }
}

