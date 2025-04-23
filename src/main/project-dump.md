### java\com\example\easy_learning\config\ApplicationConfig.java
```java
package com.example.easy_learning.config;

import com.example.easy_learning.security.JwtTokenFilter;
import com.example.easy_learning.security.UserJwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class ApplicationConfig {

  private final UserJwtUserDetailsService userDetailsService;
  private final JwtTokenFilter jwtFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder builder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
    builder.authenticationProvider(authenticationProvider());
    return builder.build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/frontend/login"))
            )
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/frontend/**").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/**").authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
```

### java\com\example\easy_learning\controller\AuthController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.auth.JwtResponse;
import com.example.easy_learning.model.Role;
import com.example.easy_learning.model.User;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final UserService userService;

  @PostMapping("/login")
  public JwtResponse login(@RequestBody @Validated JwtRequest req) {
    return authService.login(req);
  }

  @PostMapping("/register")
  public ResponseEntity<User> register(@RequestBody RegisterDto dto) {
    User user = new User();
    user.setEmail(dto.getEmail());
    user.setPassword(dto.getPassword());
    if (dto.isTutor()) {
      user.getRoles().add(Role.TUTOR);
    }
    user.getRoles().add(Role.STUDENT);
    return ResponseEntity.ok(userService.create(user));
  }
}
```

### java\com\example\easy_learning\controller\HomeworkController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.mapper.*;
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

//  private final HomeworkService homeworkService;
//  private final HomeworkMapper homeworkMapper;
//  private final TutorMapper tutorMapper;
//  private final HomeworkTaskMapper homeworkTaskMapper;
//  private final StudentsHomeworkMapper studentsHomeworkMapper;
//
//  @PostMapping
//  public ResponseEntity<Homework> createHomework(@RequestBody HomeworkRDto homeworkRDto) {
//    Homework toCreate = homeworkMapper.toNREntity(homeworkMapper.toNRDto(homeworkRDto));
//    toCreate.setTutor(tutorMapper.toNREntity(homeworkRDto.getTutor()));
//    toCreate.setTasks(homeworkTaskMapper.toEntitiesFromTDtos(homeworkRDto.getTasks()));
//    toCreate.setStudents(studentsHomeworkMapper.toEntitiesFromSDto(homeworkRDto.getStudents()));
//
//    Homework created = homeworkService.createHomework(toCreate);
//    return ResponseEntity.ok(created);
//  }
//
//  @GetMapping("/{id}")
//  public ResponseEntity<?> getHomework(@PathVariable Integer id,
//                                              @RequestParam(value = "full", defaultValue = "false") boolean full) {
//    if (full) {
//      Homework homework = homeworkService.getHomeworkWithAssociationsById(id);
//      HomeworkRDto toResponse = homeworkMapper.toRDto(homeworkMapper.toNRDto(homework));
//      toResponse.setTasks(homeworkTaskMapper.toTDtos(homework.getTasks()));
//      toResponse.setStudents(studentsHomeworkMapper.toSDtoSet(homework.getStudents()));
//      toResponse.setTutor(tutorMapper.toNRDto(homework.getTutor()));
//      return ResponseEntity.ok(toResponse);
//    }
//    else {
//      HomeworkNRDto toResponse = homeworkMapper.toNRDto(homeworkService.getHomeworkById(id));
//      return ResponseEntity.ok(toResponse);
//    }
//  }
//
//  @GetMapping
//  public ResponseEntity<Set<HomeworkNRDto>> getAllHomeworks() {
//    List<Homework> homeworks = homeworkService.getAllHomeworks();
//    Set<HomeworkNRDto> homeworkNRDtos = homeworkMapper.toNRDtos(Set.copyOf(homeworks));
//    return ResponseEntity.ok(homeworkNRDtos);
//  }
//
//  @PutMapping("/{id}")
//  public ResponseEntity<HomeworkRDto> updateHomework(@PathVariable Integer id, @RequestBody HomeworkRDto homeworkRDto) {
//    Homework toUpdate = homeworkMapper.toNREntity(homeworkMapper.toNRDto(homeworkRDto));
//    if (homeworkRDto.getTutor() != null) toUpdate.setTutor(tutorMapper.toNREntity(homeworkRDto.getTutor()));
//    if (homeworkRDto.getTasks() != null) toUpdate.setTasks(homeworkTaskMapper.toEntitiesFromTDtos(homeworkRDto.getTasks()));
//    if (homeworkRDto.getStudents() != null) toUpdate.setStudents(studentsHomeworkMapper.toEntitiesFromSDto(homeworkRDto.getStudents()));
//
//    toUpdate = homeworkService.updateHomework(id, toUpdate);
//
//    homeworkRDto = homeworkMapper.toRDto(homeworkMapper.toNRDto(toUpdate));
//    homeworkRDto.setTasks(homeworkTaskMapper.toTDtos(toUpdate.getTasks()));
//    homeworkRDto.setStudents(studentsHomeworkMapper.toSDtoSet(toUpdate.getStudents()));
//    homeworkRDto.setTutor(tutorMapper.toNRDto(toUpdate.getTutor()));
//    return ResponseEntity.ok(homeworkRDto);
//  }
//
//  @DeleteMapping("/{id}")
//  public ResponseEntity<Void> deleteHomework(@PathVariable Integer id) {
//    homeworkService.deleteHomework(id);
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Добавляет новые задачи (HomeworkTask) в домашнее задание.
//   * Принимает набор id задач в теле запроса.
//   */
//  @PostMapping("/{id}/tasks")
//  public ResponseEntity<?> addTasksToHomework(@PathVariable Integer id,
//                                                     @RequestBody Set<Integer> taskIds) {
//    try {
//      Homework updated = homeworkService.addTasksToHomework(id, taskIds);
//      return ResponseEntity.ok(updated);
//    }
//    catch (Exception e) {
//      return ResponseEntity.badRequest().body(e.getMessage());
//    }
//  }
//
//  /**
//   * Удаляет связи с задачами из домашнего задания.
//   * Принимает список id задач, связи с которыми необходимо удалить.
//   */
//  @DeleteMapping("/{id}/tasks")
//  public ResponseEntity<Homework> removeTasksFromHomework(@PathVariable Integer id,
//                                                          @RequestBody List<Integer> taskIds) {
//    Homework updated = homeworkService.removeTasksFromHomework(id, taskIds);
//    return ResponseEntity.ok(updated);
//  }
}
```

### java\com\example\easy_learning\controller\TaskController.java
```java
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
import java.util.Set;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

//  private final TaskService taskService;
//  private final TaskMapper taskMapper;
//  private final TutorMapper tutorMapper;
//  private final HomeworkTaskMapper homeworkTaskMapper;
//
//  /**
//   * Создание новой задачи без файла.
//   */
//  @PostMapping(consumes = "application/json")
//  public ResponseEntity<Task> createTask(@RequestBody TaskNRDto taskNRDto) {
//    Task toCreate = taskMapper.toNREntity(taskNRDto);
//    toCreate.setTutor(null);
//    Task created = taskService.createTask(toCreate);
//    return ResponseEntity.ok(created);
//  }
//
//  /**
//   * Создание новой задачи с загрузкой файла.
//   * JSON-часть запроса должна быть передана в поле "task",
//   * файл – в поле "file".
//   */
//  @PostMapping(value = "/with-file")
//  public ResponseEntity<Task> createTaskWithFile(@RequestPart("task") TaskNRDto taskNRDto,
//                                                 @RequestPart("file") MultipartFile file) throws IOException {
//    Task toCreate = taskMapper.toNREntity(taskNRDto);
//    toCreate.setTutor(null);
//    Task created = taskService.createTaskWithFile(toCreate, file);
//    return ResponseEntity.ok(created);
//  }
//
//  /**
//   * Получение задачи по id.
//   * Если параметр full=true, возвращаются все связи (используется метод getTaskByIdWithAllRelations).
//   */
//  @GetMapping("/{id}")
//  public ResponseEntity<?> getTask(@PathVariable Integer id,
//                                      @RequestParam(value = "full", defaultValue = "false") boolean full) {
//    Task task;
//    if (full) {
//      task = taskService.getTaskByIdWithAllRelations(id);
//      TaskRDto taskRDto = taskMapper.toRDto(taskMapper.toNRDto(task));
//      taskRDto.setTutor(tutorMapper.toNRDto(task.getTutor()));
//      taskRDto.setHomeworks(homeworkTaskMapper.toHDtos(task.getHomeworks()));
//      return ResponseEntity.ok(taskRDto);
//    }
//    else {
//      return ResponseEntity.ok(taskMapper.toNRDto(taskService.getTaskById(id)));
//    }
//  }
//
//  /**
//   * Получение списка всех задач.
//   */
//  @GetMapping
//  public ResponseEntity<Set<TaskNRDto>> getAllTasks() {
//    List<Task> tasks = taskService.getAllTasks();
//    Set<TaskNRDto> taskNRDtos = taskMapper.toNRDtos(Set.copyOf(tasks));
//    return ResponseEntity.ok(taskNRDtos);
//  }
//
//  /**
//   * Обновление задачи. Если передан новый файл, он будет обработан.
//   * JSON-часть запроса передаётся в поле "task", файл – в поле "file" (необязательный).
//   */
//  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  public ResponseEntity<TaskRDto> updateTask(@PathVariable Integer id,
//                                         @RequestPart("task") TaskNRDto taskNRDto,
//                                         @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
//    Task toUpdate = taskMapper.toNREntity(taskNRDto);
//
//    Task updated = taskService.updateTask(id, toUpdate, file);
//
//    TaskRDto taskRDto = taskMapper.toRDto(taskMapper.toNRDto(updated));
//    taskRDto.setHomeworks(homeworkTaskMapper.toHDtos(updated.getHomeworks()));
//    taskRDto.setTutor(tutorMapper.toNRDto(updated.getTutor()));
//    return ResponseEntity.ok(taskRDto);
//  }
//
//  /**
//   * Удаление задачи по id.
//   */
//  @DeleteMapping("/{id}")
//  public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
//    taskService.deleteTask(id);
//    return ResponseEntity.noContent().build();
//  }
//
//  /**
//   * Возвращает файл фотографии для задачи по её ID.
//   */
//  @GetMapping("/{id}/photo")
//  public ResponseEntity<byte[]> getTaskPhoto(@PathVariable Integer id) throws IOException {
//    byte[] photo = taskService.getTaskPhoto(id);
//    Task task = taskService.getTaskById(id);
//    String photoPath = task.getPhotoUrl();
//
//    // Определяем MIME-тип на основе расширения файла
//    String contentType = "application/octet-stream";
//    int dotIndex = photoPath.lastIndexOf('.');
//    if (dotIndex != -1) {
//      String ext = photoPath.substring(dotIndex + 1).toLowerCase();
//      if ("png".equals(ext)) {
//        contentType = "image/png";
//      } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
//        contentType = "image/jpeg";
//      } else if ("gif".equals(ext)) {
//        contentType = "image/gif";
//      }
//    }
//
//    return ResponseEntity.ok()
//            .contentType(MediaType.parseMediaType(contentType))
//            .body(photo);
//  }
}
```

### java\com\example\easy_learning\controller\ViewController.java
```java
package com.example.easy_learning.controller;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/frontend")
public class ViewController {

  @GetMapping("/tasks")
  public String tasksPage() {
    return "tasks";     // будет рендерить resources/templates/tasks.html
  }
  @GetMapping({"/login", "/login-page"})
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

```

### java\com\example\easy_learning\dto\auth\JwtRequest.java
```java
package com.example.easy_learning.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Login request payload")
public class JwtRequest {

    @Schema(description = "Email", example = "user@mail.com")
    @NotBlank(message = "Username must not be blank")//400
    private String username;

    @Schema(description = "Password", example = "12345")
    @NotBlank(message = "Password must not be blank")//400
    private String password;
}
```

### java\com\example\easy_learning\dto\auth\JwtResponse.java
```java
package com.example.easy_learning.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response containing JWT tokens")
@Builder
public class JwtResponse {
    private Integer id;
    private String username;
    private String accessToken;
    private String refreshToken;
}
```

### java\com\example\easy_learning\dto\auth\RefreshRequest.java
```java
package com.example.easy_learning.dto.auth;

import lombok.Data;

@Data
public class RefreshRequest {
  private String refreshToken;
}
```

### java\com\example\easy_learning\dto\HomeworkNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class HomeworkNRDto {

  private Integer id;

  private String className; // Используем "className" вместо "class"

  private String subject;

  private String topic;

  private Integer difficulty;
}
```

### java\com\example\easy_learning\dto\HomeworkRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

import java.util.Set;

@Data
public class HomeworkRDto {

  private Integer id;

  private String className;

  private String subject;

  private String topic;

  private Integer difficulty;

  private TutorNRDto tutor;

  private Set<StudentsHomeworkSDto> students;

  private Set<HomeworkTaskTDto> tasks;
}
```

### java\com\example\easy_learning\dto\HomeworkTaskHDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class HomeworkTaskHDto {
  private Integer id;
  private HomeworkNRDto homework;
}
```

### java\com\example\easy_learning\dto\HomeworkTaskTDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class HomeworkTaskTDto {

  private final Integer id;
  private final TaskNRDto task;
}
```

### java\com\example\easy_learning\dto\RegisterDto.java
```java
package com.example.easy_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {
    private String email;
    private String password;
    private boolean tutor;
}
```

### java\com\example\easy_learning\dto\RegisterResponse.java
```java
package com.example.easy_learning.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

  private String email;
  private String password;
  private String userType;
}
```

### java\com\example\easy_learning\dto\StudentNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class StudentNRDto {

  private Integer id;

  private String password;

  private String email;

  private StudentPersonalInfoDto studentPersonalInfo;
}

```

### java\com\example\easy_learning\dto\StudentPersonalInfoDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentPersonalInfoDto {

  private String firstname;

  private String lastname;

  private LocalDate birthdate;

  private String className;

  private String subject;

  private String phone;

  private String telegram;
}
```

### java\com\example\easy_learning\dto\StudentRDto.java
```java
package com.example.easy_learning.dto;

import com.example.easy_learning.model.StudentsHomework;
import lombok.Data;

import java.util.Set;

@Data
public class StudentRDto {

  private Integer id;

  private String password;

  private StudentPersonalInfoDto studentPersonalInfo;

  private String email;

  private Set<StudentsHomeworkHDto> homeworks;

  private Set<StudentsTutorsTDto> tutors;
}
```

### java\com\example\easy_learning\dto\StudentsHomeworkHDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class StudentsHomeworkHDto {
    private Integer id;

    private HomeworkNRDto homework;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;
}
```

### java\com\example\easy_learning\dto\StudentsHomeworkSDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentsHomeworkSDto {
    private Integer id;

    private StudentNRDto student;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;
}
```

### java\com\example\easy_learning\dto\StudentsTutorsSDto.java
```java
package com.example.easy_learning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StudentsTutorsSDto {
  private Integer id;

  private StudentNRDto student;
}
```

### java\com\example\easy_learning\dto\StudentsTutorsTDto.java
```java
package com.example.easy_learning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentsTutorsTDto {
  private Integer id;
  private TutorNRDto tutor;
}
```

### java\com\example\easy_learning\dto\TaskNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class TaskNRDto {

  private Integer id;

  private String photoUrl;

  private String className;

  private String subject;

  private String topic;

  private Integer difficulty;

  private Integer tutorId;
}
```

### java\com\example\easy_learning\dto\TaskRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

import java.util.Set;

@Data
public class TaskRDto {

  private Integer id;

  private String photoUrl;

  private String className;

  private String subject;

  private String topic;

  private Integer difficulty;

  private TutorNRDto tutor;

  private Set<HomeworkTaskHDto> homeworks;
}
```

### java\com\example\easy_learning\dto\TutorNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class TutorNRDto {

  private Integer id;

  private TutorPersonalInfoDto personalInfo;

  private String email;

  private String password;
}
```

### java\com\example\easy_learning\dto\TutorPersonalInfoDto.java
```java
package com.example.easy_learning.dto;

import java.time.LocalDate;
import lombok.*;
import lombok.Data;

@Getter
@Setter
@NoArgsConstructor
@Data
public class TutorPersonalInfoDto {

  private String firstname;

  private String lastname;

  private LocalDate birthdate;

  private String phone;

  private String telegram;
}
```

### java\com\example\easy_learning\dto\TutorRDto.java
```java
package com.example.easy_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Set;

@Data // Оставь как есть
@NoArgsConstructor
@AllArgsConstructor // <-- добавь, если будешь использовать полные конструкторы
public class TutorRDto {
  private Integer id;

  private TutorPersonalInfoDto personalInfo;

  private String password;

  private Set<TaskNRDto> tasks;

  private Set<HomeworkNRDto> homeworks;

  private String email;

  private Set<StudentNRDto> students;
}
```

### java\com\example\easy_learning\EasyLearningApplication.java
```java
package com.example.easy_learning;

import com.example.easy_learning.service.props.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EasyLearningApplication {
	public static void main(String[] args) {
		SpringApplication.run(EasyLearningApplication.class, args);
	}
}


```

### java\com\example\easy_learning\mapper\HomeworkMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.model.Homework;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {

//  Homework toNREntity(HomeworkNRDto homeworkNRDto);
//
//  HomeworkNRDto toNRDto(Homework homework);
//
//  HomeworkRDto toRDto(HomeworkNRDto homeworkNRDto);
//  HomeworkNRDto toNRDto(HomeworkRDto homeworkRDto);
//
//  List<Homework> toNREntities(List<HomeworkNRDto> homeworkNRDtos);
//
//  Set<HomeworkNRDto> toNRDtos(Set<Homework> homeworks);
//  List<HomeworkRDto> toRDtos(List<HomeworkNRDto> homeworkNRDtoss);
}
```

### java\com\example\easy_learning\mapper\HomeworkTaskMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkTaskHDto;
import com.example.easy_learning.dto.HomeworkTaskTDto;
import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, TaskMapper.class})
public interface HomeworkTaskMapper {
//  HomeworkTaskHDto toHDto(HomeworkTask homeworkTask);
//  HomeworkTaskTDto toTDto(HomeworkTask homeworkTask);
//
//  Set<HomeworkTaskHDto> toHDtos(Set<HomeworkTask> homeworkTasks);
//  Set<HomeworkTaskTDto> toTDtos(Set<HomeworkTask> homeworkTasks);
//
//  Set<HomeworkTask> toEntitiesFromHDtos(Set<HomeworkTaskHDto> homeworkTaskHDtos);
//  Set<HomeworkTask> toEntitiesFromTDtos(Set<HomeworkTaskTDto> homeworkTaskTDtos);
}
```

### java\com\example\easy_learning\mapper\StudentMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StudentPersonalInfoMapper.class})
public interface StudentMapper {

//  Student toNREntity(StudentNRDto studentNRDto);
//
//  StudentNRDto toNRDto(Student student);
//
//  StudentRDto toRDto(StudentNRDto studentNRDto);
//  StudentNRDto toNRDto(StudentRDto studentRDto);
//
//  List<Student> toNREntities(List<StudentNRDto> studentNRDtos);
//
//  List<StudentNRDto> toNRDtos(List<Student> students);
//  List<StudentRDto> toRDtos(List<StudentNRDto> studentNRDtoss);
//
//  Student toEntity(RegisterDto registerDto);
}
```

### java\com\example\easy_learning\mapper\StudentPersonalInfoMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentPersonalInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentPersonalInfoMapper {
//  StudentPersonalInfoDto toDto(StudentPersonalInfo studentPersonalInfo);
//  StudentPersonalInfo toEntity(StudentPersonalInfoDto studentPersonalInfoDto);
}
```

### java\com\example\easy_learning\mapper\StudentsHomeworkMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsHomeworkHDto;
import com.example.easy_learning.dto.StudentsHomeworkSDto;
import com.example.easy_learning.model.StudentsHomework;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, StudentMapper.class})
public interface StudentsHomeworkMapper {
//  StudentsHomeworkHDto toHDto(StudentsHomework studentsHomework);
//  StudentsHomeworkSDto toSDto(StudentsHomework studentsHomework);
//
//
//
//  Set<StudentsHomeworkHDto> toHDtoSet(Set<StudentsHomework> studentsHomeworkSet);
//  Set<StudentsHomeworkSDto> toSDtoSet(Set<StudentsHomework> studentsHomeworkSet);
//
//  Set<StudentsHomework> toEntitiesFromSDto(Set<StudentsHomeworkSDto> studentsHomeworkSet);
//  Set<StudentsHomework> toEntitiesFromHDto(Set<StudentsHomeworkHDto> studentsHomeworkSet);
}
```

### java\com\example\easy_learning\mapper\StudentsTutorsMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsTutorsSDto;
import com.example.easy_learning.dto.StudentsTutorsTDto;
import com.example.easy_learning.model.StudentsTutors;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, TutorMapper.class})
public interface StudentsTutorsMapper {
//  StudentsTutorsSDto toSDto(StudentsTutors studentsTutors);
//  StudentsTutorsTDto toTDto(StudentsTutors studentsTutors);
//
//  Set<StudentsTutorsTDto> toTDtoSet(Set<StudentsTutors> studentsTutorsSet);
//  Set<StudentsTutorsSDto> toSDtoSet(Set<StudentsTutors> studentsTutorsSet);
//
//  Set<StudentsTutors> toEntitiesFromTDto(Set<StudentsTutorsTDto> studentsTutorsSet);
//  Set<StudentsTutors> toEntitiesFromSDto(Set<StudentsTutorsSDto> studentsTutorsSet);
}
```

### java\com\example\easy_learning\mapper\TaskMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.dto.TaskRDto;
import com.example.easy_learning.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

//  @Mapping(target = "tutor.id", source = "tutorId")
//  Task toNREntity(TaskNRDto taskNRDto);
//
//  @Mapping(target = "tutorId", source = "tutor.id")
//  TaskNRDto toNRDto(Task task);
//  TaskRDto toRDto(TaskNRDto taskNRDto);
//
//  Set<Task> toNREntities(Set<TaskNRDto> taskNRDtos);
//
//  Set<TaskNRDto> toNRDtos(Set<Task> tasks);
//  Set<TaskRDto> toRDtos(Set<TaskNRDto> taskNRDtoss);
}
```

### java\com\example\easy_learning\mapper\TutorMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TutorPersonalInfoMapper.class})
public interface TutorMapper {

//  Tutor toNREntity(TutorNRDto tutorNRDto);
//  TutorRDto toRDto(Tutor tutor); // <-- этот нужен в контроллере
//  Tutor toEntity(TutorRDto tutorRDto); // <-- для обратного преобразования
//
//  @Mapping(target = "personalInfo", source = "personalInfo")
//  TutorNRDto toNRDto(Tutor tutor);
//  TutorNRDto toNRDto(TutorRDto tutor);
//  TutorRDto toRDto(TutorNRDto tutorNRDto);
//
//  Set<Tutor> toNREntities(Set<TutorNRDto> tutorNRDtos);
//
//  Set<TutorNRDto> toNRDtos(Set<Tutor> tutors);
//  Set<TutorRDto> toRDtos(Set<TutorNRDto> tutorNRDtoss);
//
//  Tutor toTutor(RegisterDto registerDto);
}
```

### java\com\example\easy_learning\mapper\TutorPersonalInfoMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TutorPersonalInfoDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TutorPersonalInfoMapper {

//  TutorPersonalInfoDto toTutorPersonalInfoDto(TutorPersonalInfo tutorPersonalInfo);
//  TutorPersonalInfo toTutorPersonalInfo(TutorPersonalInfoDto tutorPersonalInfoDto);
}
```

### java\com\example\easy_learning\model\ClassLevel.java
```java
package com.example.easy_learning.model;

public enum ClassLevel {
  CLASS_1,
  CLASS_2,
  CLASS_3,
  CLASS_4,
  CLASS_5,
  CLASS_6,
  CLASS_7,
  CLASS_8,
  CLASS_9,
  CLASS_10,
  CLASS_11
}
```

### java\com\example\easy_learning\model\Homework.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "homework")
@Getter
@Setter
@NoArgsConstructor
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "class", nullable = false)
    private ClassLevel className; // Используем "className" вместо "class"

    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false)
    private Subject subject;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @OneToMany(mappedBy = "homework", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsHomework> students = new HashSet<>();

    @OneToMany(mappedBy = "homework", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HomeworkTask> tasks = new HashSet<>();

    public void setTasks(Set<HomeworkTask> tasks) {
        this.tasks.clear();
        tasks.forEach(task -> task.setHomework(this));
    }

    public void setStudents(Set<StudentsHomework> students) {
        this.students.clear();
        students.forEach(student -> student.setHomework(this));
    }
}
```

### java\com\example\easy_learning\model\HomeworkTask.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.*;

@Entity
@Table(name = "homework_task")
@Getter
@Setter
@NoArgsConstructor
public class HomeworkTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    public void setTask(Task task) {
        this.task = task;
        if (task != null && task.getHomeworks() != null) task.getHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null && homework.getTasks() != null) homework.getTasks().add(this);
    }
}
```

### java\com\example\easy_learning\model\PersonalInfo.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PersonalInfo {

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "telegram", unique = true)
    private String telegram;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_subject_classes",
        joinColumns = @JoinColumn(name = "user_id")
    )
    private Set<SubjectClassPair> subjectClassPairs;
}
```

### java\com\example\easy_learning\model\Role.java
```java
package com.example.easy_learning.model;

public enum Role {
    STUDENT,
    TUTOR
}
```

### java\com\example\easy_learning\model\StudentsHomework.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Data;

@Entity
@Table(name = "students_homework")
@Getter
@Setter
@NoArgsConstructor
public class StudentsHomework {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;

    public void setStudent(User student) {
        this.student = student;
        if (student != null && student.getHomeworks() != null) student.getStudentHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null && homework.getStudents() != null) homework.getStudents().add(this);
    }
}
```

### java\com\example\easy_learning\model\StudentsTutors.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Getter;

@Entity
@Table(name = "students_tutors")
@Getter
@Setter
@NoArgsConstructor
public class StudentsTutors {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne
  @JoinColumn(name = "tutor_id")
  private User tutor;

  public void setStudent(User student) {
    this.student = student;
    if (student != null && student.getTutors() != null) student.getTutors().add(this);
  }

  public void setTutor(User tutor) {
    this.tutor = tutor;
    if (tutor != null && tutor.getStudents() != null) tutor.getStudents().add(this);
  }
}
```

### java\com\example\easy_learning\model\Subject.java
```java
package com.example.easy_learning.model;

public enum Subject {
    MATH,
    PHYSICS,
    CHEMISTRY,
    BIOLOGY,
    LITERATURE,
    HISTORY,
    RUSSIAN,
    ENGLISH
}
```

### java\com\example\easy_learning\model\SubjectClassPair.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SubjectClassPair {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_level")
    private ClassLevel classLevel;
}
```

### java\com\example\easy_learning\model\Task.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "class", nullable = false)
    private String className; // Используем "className" вместо "class"

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HomeworkTask> homeworks = new HashSet<>();
}
```

### java\com\example\easy_learning\model\User.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Set<Role> roles = new HashSet<>();

  // Встраиваемая персональная информация
  @Embedded
  private PersonalInfo personalInfo;

  // Задания, созданные репетитором
  @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Task> tasks = new HashSet<>();

  // Домашние задания, созданные репетитором
  @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Homework> homeworks = new HashSet<>();

  // Связи ученик–домзадание
  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StudentsHomework> studentHomeworks = new HashSet<>();

  // Связи ученик–тьютор (роль STUDENT)
  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StudentsTutors> tutors = new HashSet<>();

  @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StudentsTutors> students = new HashSet<>();
}
```

### java\com\example\easy_learning\repository\HomeworkRepository.java
```java
package com.example.easy_learning.repository;

import com.example.easy_learning.model.Homework;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HomeworkRepository extends JpaRepository<Homework, Integer> {

    @Query("SELECT h FROM Homework h WHERE h.id = :id")
    @EntityGraph(attributePaths = {
            "tutor",
            "students",
            "students.student",
            "tasks",
            "tasks.task"
    })
    Optional<Homework> findHomeworkWithAssociationsById(@Param("id") Integer id);
}
```

### java\com\example\easy_learning\repository\TaskRepository.java
```java
package com.example.easy_learning.repository;

import com.example.easy_learning.model.Task;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer> {

  @EntityGraph(attributePaths = {"homeworks", "homeworks.homework","tutor"})
  @Query("SELECT t FROM Task t WHERE t.id = :id")
  Optional<Task> findByIdWithAllRelations(@Param("id") Integer id);
}
```

### java\com\example\easy_learning\repository\UserRepository.java
```java
package com.example.easy_learning.repository;

import com.example.easy_learning.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
```

### java\com\example\easy_learning\security\JwtTokenFilter.java
```java
package com.example.easy_learning.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String bearerToken = ((HttpServletRequest) servletRequest).getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {//Получаем заголовок Authorization
            bearerToken = bearerToken.substring(7);
        }

        try {//Получаем объект Authentication, в котором содержится пользователь,
            if (bearerToken != null && jwtTokenProvider.validate(bearerToken)) { //Проверяем: токен есть и он валидный
                Authentication authentication = jwtTokenProvider.getAuthentication(bearerToken);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }//Вставляем Authentication в контекст безопасности, чтобы Spring знал: "этот пользователь авторизован"
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(servletRequest, servletResponse);
        //Передаём управление дальше — другим фильтрам, контроллерам и т.д.
    }
}
```

### java\com\example\easy_learning\security\JwtTokenProvider.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.service.props.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties props;
    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
    }

    public String createAccessToken(Authentication auth) {
        UserJwtEntity user = (UserJwtEntity) auth.getPrincipal();
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("id", user.getId())
            .claim("roles", user.getAuthorities())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(props.getAccess(), ChronoUnit.HOURS)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String createRefreshToken(Authentication auth) {
        UserJwtEntity user = (UserJwtEntity) auth.getPrincipal();
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("id", user.getId())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(props.getRefresh(), ChronoUnit.DAYS)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validate(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        var claims = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        UserDetailsService uds = new UserJwtUserDetailsService(null); // injected by Spring normally
        UserDetails userDetails = uds.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
```

### java\com\example\easy_learning\security\UserJwtEntity.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class UserJwtEntity implements UserDetails {
    private final Integer id;
    private final String username;
    private final String password;
    private final Collection<GrantedAuthority> authorities;

    public UserJwtEntity(User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toList());
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
```

### java\com\example\easy_learning\security\UserJwtUserDetailsService.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.model.User;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserJwtUserDetailsService implements UserDetailsService {
    private final UserService userService;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new UserJwtEntity(user);
    }
}
```

### java\com\example\easy_learning\service\AuthService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.auth.JwtResponse;
import org.springframework.stereotype.Service;


@Service
public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(String refreshToken);
}
```

### java\com\example\easy_learning\service\HomeworkService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Homework;

import java.util.List;
import java.util.Set;

public interface HomeworkService {
  Homework createHomework(Homework homework);
  Homework getHomeworkById(Integer id);
  Homework getHomeworkWithAssociationsById(Integer id);
  Homework updateHomework(Integer id, Homework updatedHomework);
  void deleteHomework(Integer id);
  List<Homework> getAllHomeworks();
  Homework addTasksToHomework(Integer homeworkId, Set<Integer> taskIds);
  Homework removeTasksFromHomework(Integer homeworkId, List<Integer> taskIds);
}
```

### java\com\example\easy_learning\service\impl\AuthServiceImpl.java
```java
package com.example.easy_learning.service.impl;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.auth.JwtResponse;
import com.example.easy_learning.model.User;
import com.example.easy_learning.security.JwtTokenProvider;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authManager;
    private final UserService userService;
    private final JwtTokenProvider jwtProvider;

    @Override
    public JwtResponse login(JwtRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = userService.findByEmail(req.getUsername()).orElseThrow();
        return JwtResponse.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(jwtProvider.createAccessToken(auth))
                .refreshToken(jwtProvider.createRefreshToken(auth))
                .build();
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        if (!jwtProvider.validate(refreshToken))
            throw new RuntimeException("Invalid refresh token");
        Authentication auth = jwtProvider.getAuthentication(refreshToken);
        String access = jwtProvider.createAccessToken(auth);
        return JwtResponse.builder()
                .accessToken(access)
                .refreshToken(refreshToken)
                .build();
    }
}
```

### java\com\example\easy_learning\service\impl\HomeworkServiceImpl.java
```java
package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.TaskRepository;
import com.example.easy_learning.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl implements HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final TaskRepository taskRepository;


    @Override
    public Homework createHomework(Homework homework) {
        return homeworkRepository.save(homework);
    }

    @Override
    public Homework getHomeworkWithAssociationsById(Integer id) {
        return homeworkRepository.findHomeworkWithAssociationsById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found"));
    }

    @Override
    public List<Homework> getAllHomeworks() {
        return homeworkRepository.findAll();
    }

    @Override
    public Homework updateHomework(Integer id, Homework updatedHomework) {
        Homework existing = getHomeworkWithAssociationsById(id);
        existing.setClassName(updatedHomework.getClassName());
        existing.setSubject(updatedHomework.getSubject());
        existing.setTopic(updatedHomework.getTopic());
        existing.setDifficulty(updatedHomework.getDifficulty());
        existing.setTutor(updatedHomework.getTutor());
        existing.setStudents(updatedHomework.getStudents());
        existing.setTasks(updatedHomework.getTasks());
        return homeworkRepository.save(existing);
    }

    @Override
    public void deleteHomework(Integer id) {
        homeworkRepository.deleteById(id);
    }

    @Override
    public Homework removeTasksFromHomework(Integer homeworkId, List<Integer> taskIds) {
        Homework homework = getHomeworkWithAssociationsById(homeworkId);

        homework.getTasks().removeIf(ht -> {
            if (taskIds.contains(ht.getTask().getId())) {
                ht.setHomework(null);
                return true;
            }
            return false;
        });

        return homeworkRepository.save(homework);
    }
    @Override
    public Homework getHomeworkById(Integer id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found with id: " + id));
    }
    @Override
    public Homework addTasksToHomework(Integer homeworkId, Set<Integer> taskIds) {
        Homework homework = homeworkRepository.findHomeworkWithAssociationsById(homeworkId)
                .orElseThrow(() -> new RuntimeException("Homework not found with id: " + homeworkId));

        for (Integer taskId : taskIds) {
            boolean exists = homework.getTasks().stream()
                    .anyMatch(ht -> ht.getTask().getId().equals(taskId));
            if (exists) {
                throw new RuntimeException("Task with id " + taskId + " already exists in Homework");
            }

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task with id " + taskId + " not found"));
            HomeworkTask ht = new HomeworkTask();
            ht.setTask(task);
            ht.setHomework(homework);
        }

        return homeworkRepository.save(homework);
    }

}
```

### java\com\example\easy_learning\service\impl\TaskServiceImpl.java
```java
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
```

### java\com\example\easy_learning\service\impl\UserServiceImpl.java
```java
package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.User;
import com.example.easy_learning.repository.UserRepository;
import com.example.easy_learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Сохраняет пользователя с захешированным паролем.
     */
    @Override
    @Transactional
    public User create(User user) {
        // Хешируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Ищет пользователя по email.
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
```

### java\com\example\easy_learning\service\props\JwtProperties.java
```java
package com.example.easy_learning.service.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private long access;
    private long refresh;
}
```

### java\com\example\easy_learning\service\TaskService.java
```java
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
```

### java\com\example\easy_learning\service\UserService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.User;
import java.util.Optional;

public interface UserService {
    // Создать нового пользователя
    User create(User user);

    // Найти пользователя по email
    Optional<User> findByEmail(String email);
}
```

### resources\application.yml
```yaml
spring:
  datasource:
#    url: jdbc:mysql://mysql:3306/easy_learning
    url: jdbc:mysql://localhost:2020/easy_learning
    username: root
    password: 12345
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
#    open-in-view: false
  application:
    name: easy_learning
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true

security:
  jwt:
#    secret: ${JWT_SECRET}
    secret: ZGNweGNwbGN4eHh4eHh4eHhzc3Nzc3Nzc3NzeA==
    access: 30
    refresh: 300
```

### resources\db\changelog\db.changelog-master.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <!-- v2 – актуальная схема под Entity -->
    <include file="v1/db.changelog-v1.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

### resources\db\changelog\v1\db.changelog-v1.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <!-- Core tables -->
    <include file="users.xml" relativeToChangelogFile="true"/>
    <include file="homework.xml" relativeToChangelogFile="true"/>
    <include file="task.xml" relativeToChangelogFile="true"/>
    <include file="homework_task.xml" relativeToChangelogFile="true"/>
    <include file="student_homework.xml" relativeToChangelogFile="true"/>
    <include file="student_tutors.xml" relativeToChangelogFile="true"/>

    <!-- Тестовые данные, если нужно -->
<!--    <include file="test-data.xml" relativeToChangelogFile="true"/>-->
</databaseChangeLog>
```

### resources\db\changelog\v1\homework.sql
```sql
CREATE TABLE homework
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    class      VARCHAR(50)  NOT NULL,
    subject    VARCHAR(50)  NOT NULL,
    topic      VARCHAR(255) NOT NULL,
    difficulty INT          NOT NULL,
    tutor_id   INT          NOT NULL,
    CONSTRAINT fk_homework_tutor FOREIGN KEY (tutor_id) REFERENCES users (id)
);
```

### resources\db\changelog\v1\homework.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-homework-table" author="yourName">
        <sqlFile path="homework.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\db\changelog\v1\homework_task.sql
```sql
CREATE TABLE homework_task (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    homework_id INT,
    task_id     INT,
    CONSTRAINT fk_ht_homework FOREIGN KEY (homework_id) REFERENCES homework(id) ON DELETE CASCADE,
    CONSTRAINT fk_ht_task     FOREIGN KEY (task_id)     REFERENCES task(id)      ON DELETE CASCADE
);
```

### resources\db\changelog\v1\homework_task.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-homework_task-table" author="yourName">
        <sqlFile path="homework_task.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\db\changelog\v1\student_homework.sql
```sql
CREATE TABLE students_homework
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT,
    homework_id INT,
    is_done     BOOLEAN,
    is_checked  BOOLEAN,
    score       INT,
    CONSTRAINT fk_sh_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_sh_homework FOREIGN KEY (homework_id) REFERENCES homework (id) ON DELETE CASCADE
);
```

### resources\db\changelog\v1\student_homework.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-students_homework-table" author="yourName">
        <sqlFile path="student_homework.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\db\changelog\v1\student_tutors.sql
```sql
CREATE TABLE students_tutors
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    tutor_id   INT,
    CONSTRAINT fk_st_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_st_tutor FOREIGN KEY (tutor_id) REFERENCES users (id) ON DELETE CASCADE
);
```

### resources\db\changelog\v1\student_tutors.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-students_tutors-table" author="yourName">
        <sqlFile path="student_tutors.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\db\changelog\v1\task.sql
```sql
CREATE TABLE task
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    photo_url  VARCHAR(255) NOT NULL,
    class      VARCHAR(50)  NOT NULL,
    subject    VARCHAR(50)  NOT NULL,
    topic      VARCHAR(255) NOT NULL,
    difficulty INT          NOT NULL,
    tutor_id   INT,
    CONSTRAINT fk_task_tutor FOREIGN KEY (tutor_id) REFERENCES users (id)
);
```

### resources\db\changelog\v1\task.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-task-table" author="yourName">
        <sqlFile path="task.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\db\changelog\v1\test-data.sql
```sql
-- Вставка репетиторов
INSERT INTO tutor (password, firstname, lastname, birthdate, email, phone, telegram)
VALUES ('$2a$12$lvTdIVYSAlvhM2PbMKJJGOCXl.MuhtUNl3kUZAbI8FKsb/8ei1BIa', 'Alice', 'Anderson', '1975-06-15', 'alice.anderson@example.com', '1111111111', 'alice_a'),
       ('$2a$12$2StXDXvy7wq4XoIudYxlpue626sqFAaGzcyOfVQ6wbQq3CE5P/QCW', 'Bob', 'Brown', '1980-09-20', 'bob.brown@example.com', '2222222222', 'bob_b');

-- Вставка учеников
INSERT INTO student (password, firstname, lastname, birthdate, class, subject, email, phone, telegram)
VALUES ('$2a$12$DMWwMhm25MTKlT1ZPR4DTutLEfxsrQ1gdnaQadAWYbRzf7xrKIM02', 'Charlie', 'Clark', '2005-03-10', '10A', 'Math', 'charlie@example.com', '3333333333', 'charlie_c'),
       ('$2a$12$5PpIabi.CLFHrxxzC5vRtuOfHw4FbbqtwlCJzXFzDLh0h9ji66yj6', 'Diana', 'Davis', '2006-07-22', '10B', 'Physics', 'diana@example.com', '4444444444', 'diana_d');

-- Вставка домашних заданий
INSERT INTO homework (class, subject, topic, difficulty, tutor_id)
VALUES ('10A', 'Math', 'Algebra', 3, 1),
       ('10B', 'Physics', 'Mechanics', 4, 2),
       ('10A', 'Chemistry', 'Elements', 3, 1);

-- Вставка заданий
INSERT INTO task (photo_url, class, subject, topic, difficulty, tutor_id) VALUES
  ('uploads/task1.jpg', '10A', 'Math', 'Algebra basics', 2, 1),
  ('uploads/task2.jpg', '10A', 'Math', 'Equations', 3, 1),
  ('uploads/task3.jpg', '10B', 'Physics', 'Newton Laws', 4, 2),
  ('uploads/task4.jpg', '10B', 'Physics', 'Thermodynamics', 3, 2),
  ('uploads/task5.jpg', '10B', 'Physics', 'Optics', 2, 2),
  ('uploads/task6.jpg', '10A', 'Chemistry', 'Periodic Table', 3, 1),
  ('uploads/task7.jpg', '10A', 'Chemistry', 'Chemical Reactions', 4, 1);

-- Вставка связи домашних заданий с заданиями
INSERT INTO homework_task (homework_id, task_id)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (3, 6),
       (3, 7);

-- Вставка домашних заданий для учеников
INSERT INTO students_homework (student_id, homework_id, is_done, is_checked, score)
VALUES (1, 1, TRUE, FALSE, 90),
       (1, 2, FALSE, FALSE, 0),
       (2, 3, TRUE, TRUE, 85);

-- Вставка связей учеников с репетиторами
INSERT INTO students_tutors (student_id, tutor_id)
VALUES (1, 1),
       (2, 2);
```

### resources\db\changelog\v1\test-data.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="insert-test-data" author="yourName">
        <sqlFile path="test-data.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\db\changelog\v1\users.sql
```sql
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,

    firstname     VARCHAR(255),
    lastname      VARCHAR(255),
    birthdate     DATE,
    phone         VARCHAR(255) UNIQUE,
    telegram      VARCHAR(255) UNIQUE
);

CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_subject_classes (
    user_id      INT NOT NULL,
    subject      VARCHAR(50) NOT NULL,
    class_level  VARCHAR(50) NOT NULL,
    PRIMARY KEY(user_id, subject, class_level),
    CONSTRAINT fk_user_sc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_sc_user ON user_subject_classes(user_id);
```

### resources\db\changelog\v1\users.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-users-and-collections" author="refactor">
        <sqlFile path="users.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### resources\static\css\main.css
```css
#стили
```

### resources\static\favicon.ico
```
```

### resources\static\js\app.js
```javascript
async function postJson(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function authFetch(url, opts = {}) {
  const access = localStorage.getItem('accessToken');
  opts.headers = {...(opts.headers || {}), 'Authorization': `Bearer ${access}`};
  return fetch(url, opts);
}

// Регистрация + автологин
const regForm = document.getElementById('registerForm');
if (regForm) {
  regForm.addEventListener('submit', async e => {
    e.preventDefault();
    const data = {
      email: regForm.email.value,
      password: regForm.password.value,
      tutor: regForm.tutor.checked
    };
    await postJson('/api/v1/auth/register', data);
    const tokens = await postJson('/api/v1/auth/login', {
      username: data.email,
      password: data.password
    });
    localStorage.setItem('accessToken',  tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    window.location.href = '/';
  });
}

// Логин
const loginForm = document.getElementById('loginForm');
if (loginForm) {
  loginForm.addEventListener('submit', async e => {
    e.preventDefault();
    const payload = {
      username: loginForm.username.value,
      password: loginForm.password.value
    };
    const tokens = await postJson('/api/v1/auth/login', payload);
    localStorage.setItem('accessToken',  tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    window.location.href = '/';
  });
}

// Logout‑кнопка
window.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logoutBtn');
  if (!logoutBtn) return;
  const hasToken = !!localStorage.getItem('accessToken');
  logoutBtn.classList.toggle('d-none', !hasToken);
  logoutBtn.addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/login';
  });
});
```

### resources\templates\index.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">
<head><title th:text="'Главная'"></title></head>
<body>
<section layout:fragment="content">
  <div class="text-center py-5">
    <h1 class="display-4">Добро пожаловать в EasyLearning!</h1>
    <p class="lead">Вы успешно вошли в систему. Функционал появится позже 😉</p>
  </div>
</section>
</body>
</html>
```

### resources\templates\layout.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8"/>
    <title layout:title-pattern="EasyLearning :: [#{title}]">EasyLearning</title>
    <link rel="stylesheet" th:href="@{/css/main.css}"/>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-..." crossorigin="anonymous">
</head>
<body>
<script th:inline="javascript">
    const publicPaths = ['/frontend/login', '/frontend/register'];
    const path = window.location.pathname;
    if (!publicPaths.includes(path) && !localStorage.getItem('accessToken')) {
        window.location.href = '/frontend/login';
    }
</script>
<header class="navbar navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <a class="navbar-brand" th:href="@{/frontend}">EasyLearning</a>

        <ul class="navbar-nav me-auto mb-2 mb-lg-0 d-flex flex-row">
            <!-- Ссылка “Задачи” видна только тем, у кого есть роль TUTOR -->
            <li class="nav-item me-3" sec:authorize="hasRole('TUTOR')">
                <a class="nav-link text-white" th:href="@{/frontend/tasks}">Задачи</a>
            </li>
        </ul>

        <button id="logoutBtn" class="btn btn-outline-light btn-sm d-none">Выйти</button>
    </div>
</header>
<main class="container" layout:fragment="content"></main>
<footer class="text-center text-muted py-3">© 2025</footer>

<!-- Bootstrap JS bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-..."
        crossorigin="anonymous"></script>
<!-- ваш скрипт -->
<script th:src="@{/js/app.js}" type="module"></script>
</body>
</html>
```

### resources\templates\login.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">
<head><title th:text="'Вход'"></title></head>
<body>
<section layout:fragment="content">
    <h2 class="mb-3">Вход</h2>
    <form id="loginForm" method="post" class="vstack gap-2 col-md-4 mx-auto">
        <input class="form-control" type="email" name="username" placeholder="Email" required>
        <input class="form-control" type="password" name="password" placeholder="Пароль" required>
        <button class="btn btn-primary" type="submit">Войти</button>
    </form>
    <p class="mt-3 text-center">Нет аккаунта? <a th:href="@{/frontend/register}">Регистрация</a></p>
</section>
</body>
</html>
```

### resources\templates\register.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">
<head><title th:text="'Регистрация'"></title></head>
<body>
<section layout:fragment="content">
    <h2 class="mb-3">Регистрация</h2>
    <form id="registerForm" method="post" class="vstack gap-2 col-md-4 mx-auto">
        <input class="form-control" type="email"   name="email"    placeholder="Email" required>
        <input class="form-control" type="password" name="password" placeholder="Пароль" required>
        <div class="form-check">
          <input class="form-check-input" type="checkbox" name="tutor" id="tutorChk">
          <label class="form-check-label" for="tutorChk">Я репетитор</label>
        </div>
        <button class="btn btn-success" type="submit">Зарегистрироваться</button>
    </form>
    <p class="mt-3 text-center"><a th:href="@{/frontend/login}">Уже есть аккаунт? Войти</a></p>
</section>
</body>
</html>
```

### resources\templates\tasks.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">
<head>
    <title>Задачи</title>
</head>
<body>
<section layout:fragment="content">
    <h1>Страница задач</h1>
    <p>Здесь будет список задач для репетитора.</p>
</section>
</body>
</html>
```

