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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
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

  // Страница «Мои репетиторы»
  @PreAuthorize("hasRole('STUDENT')")
  @GetMapping("/tutors")
  public String tutorsPage(Model model, Authentication auth) {
    //берёт текущего пользователя (приведённого к вашему UserJwtEntity) и его id
    Integer studentId = ((UserJwtEntity) auth.getPrincipal()).getId();
    //Запрашивает у сервиса список его репетиторови кладёт в модель tutors,Возвращает шаблон tutors.html
    model.addAttribute("tutors", studentsTutorsService.getTutorsForStudent(studentId));
    return "tutors";
  }

  // Просмотр одного репетитора
  @PreAuthorize("hasRole('STUDENT')")
  @GetMapping("/tutors/{id}")
  public String viewTutor(@PathVariable Integer id, Model model) {
    //Через userService загружает из БД репетитора по id
    User tutor = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("Tutor not found: " + id));
    model.addAttribute("tutor", tutor);
    return "tutor_view"; //Возвращает шаблон tutor_view.html
  }

  // Просмотр персональных данных
  @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
  @GetMapping("/profile")
  public String profileForm(Model model, Authentication auth) {
    // получаем текущего пользователя
    String email = auth.getName();
    // читаем email из Authentication
    User user = userService.findByEmail(email).orElseThrow();
    // собираем DTO с полями для формы
    ProfileDto dto = new ProfileDto();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setFirstname(user.getPersonalInfo() != null ? user.getPersonalInfo().getFirstname() : null);
    dto.setLastname(user.getPersonalInfo() != null ? user.getPersonalInfo().getLastname() : null);
    dto.setBirthdate(user.getPersonalInfo() != null ? user.getPersonalInfo().getBirthdate() : null);
    dto.setPhone(user.getPersonalInfo() != null ? user.getPersonalInfo().getPhone() : null);
    dto.setTelegram(user.getPersonalInfo() != null ? user.getPersonalInfo().getTelegram() : null);

    model.addAttribute("profile", dto);
    return "profile"; //кладёт его в модель под "profile" и отдаёт форму profile.html
  }

  //Сохранение профиля
  @PostMapping("/profile")
  public String updateProfile(
          @Valid @ModelAttribute("profile") ProfileDto dto,
          BindingResult bindingResult,
          RedirectAttributes redirect,
          Model model,
          HttpServletResponse response
  ) {
    if (bindingResult.hasErrors()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Устанавливаем статус 400
      model.addAttribute("profile", dto);
      return "profile"; // Возвращаем форму с ошибками
    }
    userService.updateProfile(dto);
    redirect.addFlashAttribute("success", "Данные сохранены");
    return "redirect:/frontend/profile";
  }


  // Список студентов
  @PreAuthorize("hasRole('TUTOR')")
  @GetMapping("/students")
  public String studentsPage(Model model, Authentication auth) {
    // получаем id репетитора из аутентификации
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId();
    model.addAttribute("students", studentsTutorsService.getStudentsForTutor(tutorId));
    return "students"; //кладёт список его студентов в "students" и возвращает students.html
  }

  // Добавить студента
  @PostMapping("/students")
  public String addStudent(
          @RequestParam("studentId") Integer studentId,
          Authentication auth,
          RedirectAttributes redirect,
          HttpServletResponse response
  ) {
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId();
    try {
      studentsTutorsService.addStudentToTutor(tutorId, studentId);
    } catch (NoSuchElementException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST); //400
      redirect.addFlashAttribute("error", "Студент с ID " + studentId + " не найден");
    } catch (IllegalStateException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
      redirect.addFlashAttribute("error", "Студент уже добавлен!");
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//400
      redirect.addFlashAttribute("error", "Ошибка: " + e.getMessage());
    }
    return "redirect:/frontend/students";
  }



  // Просмотр одной задачи
  @GetMapping("/tasks/{id}")
  public String viewTask(@PathVariable Integer id, Model model) {
    Task task = taskService.getTaskById(id); //загружает задачу по id
    model.addAttribute("task", task); //кладёт в модель "task" и рендерит task_view.html
    return "task_view";
  }

  // Форма создания задачи
  @PreAuthorize("hasRole('TUTOR')")
  @GetMapping("/new")
  public String newTaskForm(Model model) {
    model.addAttribute("taskForm", new TaskNRDto());              // пустой объект для биндинга формы
    model.addAttribute("classLevels", ClassLevel.values());       // список всех классов для выпадающего списка
    model.addAttribute("subjects", Subject.values());             // список всех предметов для выпадающего списка
    return "task_new";                                            // возвращаем шаблон task_new.html
  }


  // Создать задачу
  @PreAuthorize("hasRole('TUTOR')")
  @PostMapping("/new")
  public String createTask(
          @ModelAttribute TaskNRDto taskForm,
          @RequestParam("file") MultipartFile file,
          Authentication auth //Принимает DTO + файл из формы
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
    return "redirect:/frontend/tasks"; //Редиректит на список задач
  }

  // Список задач
  @PreAuthorize("hasRole('TUTOR')")
  @GetMapping("/tasks")
  public String tasksPage(
          @RequestParam(value = "sort", required = false) String sort,
          Model model,
          Authentication auth //Берёт все задачи репетитора, сортирует по полю, заданному в sort
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
    model.addAttribute("studentsList", students); //Кладёт в модель groupedTasks, sort и список студентов

    return "tasks";
  }

  // Назначить задачу студенту
  @PostMapping("/tasks/assign")
  @PreAuthorize("hasRole('TUTOR')")
  public String assignTask(@RequestParam Integer taskId,
                           @RequestParam Integer studentId,
                           Authentication auth,
                           RedirectAttributes redirect) {
    Integer tutorId = ((UserJwtEntity) auth.getPrincipal()).getId(); //tutorId из auth
    studentsTutorsService.assignTaskToStudent(tutorId, taskId, studentId);//ривязываетзадачу taskId к студенту studentId
    redirect.addFlashAttribute("assignSuccess", "Задача назначена студенту");
    //добавляет временное сообщение, которое будет доступно на следующей странице (в model).
    return "redirect:/frontend/tasks"; //редирект на страницу со списком задач репетитора.
  }

  // Главная страница
  @GetMapping
  public String home(Model model, Authentication auth) {
    //  проверяем, залогинен ли пользователь, и есть ли у него роль STUDENT
    if (auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
      Integer studentId = ((UserJwtEntity) auth.getPrincipal()).getId();
      List<Task> myTasks = studentsTasksRepo //грузим его список задач через studentsTasksRepo
              .findByStudent_Id(studentId).stream()
              .map(StudentsTasks::getTask)
              .toList();
      model.addAttribute("myTasks", myTasks);
    }
    return "index";
  }

  // Страница логина
  @GetMapping("/login")
  public String loginPage() {
    return "login";   // src/main/resources/templates/login.html
  }

  // Страница регистрации
  @GetMapping("/register")
  public String registerPage() {
    return "register"; // templates/register.html
  }
}

