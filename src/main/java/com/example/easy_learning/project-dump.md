### config\ApplicationConfig.java
```java
package com.example.easy_learning.config;

import com.example.easy_learning.security.JwtTokenFilter;
import com.example.easy_learning.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class ApplicationConfig {

    private final JwtTokenProvider tokenProvider;
    private final ApplicationContext applicationContext;

    @Bean
    public PasswordEncoder passwordEncoder() { //хэширование паролей
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SneakyThrows
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration configuration
    ) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @SneakyThrows
    public SecurityFilterChain filterChain(
            final HttpSecurity httpSecurity
    ) {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                .exceptionHandling(configurer ->
                        configurer.authenticationEntryPoint(
                                        (request,
                                         response, exception) -> {
                                            response.setStatus(
                                                    HttpStatus.UNAUTHORIZED //401
                                                            .value()
                                            );
                                            response.getWriter()
                                                    .write("Unauthorized.");
                                        })
                                .accessDeniedHandler(
                                        (request,
                                         response, exception) -> {
                                            response.setStatus(
                                                    HttpStatus.FORBIDDEN
                                                            .value()
                                            );
                                            response.getWriter()
                                                    .write("Unauthorized.");
                                        }))
                .authorizeHttpRequests(configurer ->
                        configurer.
                                requestMatchers("/api/v1/auth/**")//разрешает все такие запросы
                                .permitAll()
                                .requestMatchers("/swagger-ui/**")
                                .permitAll()
                                .requestMatchers("/v3/api-docs/**")
                                .permitAll()
                                .anyRequest().authenticated())
                .anonymous(AbstractHttpConfigurer::disable) //отключает возможность анонимного посещения
                .addFilterBefore(new JwtTokenFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}
```

### controller\AuthController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.RegisterResponse;
import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.JwtResponse;
import com.example.easy_learning.mapper.StudentMapper;
import com.example.easy_learning.mapper.TutorMapper;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.StudentService;
import com.example.easy_learning.service.TutorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ResourceBundle;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final StudentMapper studentMapper;
    private final TutorMapper tutorMapper;
    private final StudentService studentService;
    private final TutorService tutorService;

    @PostMapping("/login")
    public JwtResponse login(@RequestBody @Validated JwtRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register/{user-type}")
    public ResponseEntity<?> register(@PathVariable("user-type") String userType, @RequestBody RegisterDto registerDto) {
        if ("student".equals(userType)) {
            Student student = studentMapper.toEntity(registerDto);
            Student created = studentService.createStudent(student);
            return ResponseEntity.ok(RegisterResponse.builder().email(created.getEmail()).password(created.getPassword()).userType(userType).build());
        }
        if ("tutor".equals(userType)) {
            Tutor tutor = tutorMapper.toTutor(registerDto);
            Tutor created = tutorService.create(tutor);
            return ResponseEntity.ok(RegisterResponse.builder().email(created.getEmail()).password(created.getPassword()).userType(userType).build());
        }
        return ResponseEntity.badRequest().body("Нельзя создать пользователя с таким user type");
    }

    @PostMapping("/refresh")
    public JwtResponse refresh(@RequestBody String refreshToken) {
        return authService.refresh(refreshToken);
    }
}
```

### controller\HomeworkController.java
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

  private final HomeworkService homeworkService;
  private final HomeworkMapper homeworkMapper;
  private final TutorMapper tutorMapper;
  private final HomeworkTaskMapper homeworkTaskMapper;
  private final StudentsHomeworkMapper studentsHomeworkMapper;

  @PostMapping
  public ResponseEntity<Homework> createHomework(@RequestBody HomeworkRDto homeworkRDto) {
    Homework toCreate = homeworkMapper.toNREntity(homeworkMapper.toNRDto(homeworkRDto));
    toCreate.setTutor(tutorMapper.toNREntity(homeworkRDto.getTutor()));
    toCreate.setTasks(homeworkTaskMapper.toEntitiesFromTDtos(homeworkRDto.getTasks()));
    toCreate.setStudents(studentsHomeworkMapper.toEntitiesFromSDto(homeworkRDto.getStudents()));

    Homework created = homeworkService.createHomework(toCreate);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getHomework(@PathVariable Integer id,
                                              @RequestParam(value = "full", defaultValue = "false") boolean full) {
    if (full) {
      Homework homework = homeworkService.getHomeworkWithAssociationsById(id);
      HomeworkRDto toResponse = homeworkMapper.toRDto(homeworkMapper.toNRDto(homework));
      toResponse.setTasks(homeworkTaskMapper.toTDtos(homework.getTasks()));
      toResponse.setStudents(studentsHomeworkMapper.toSDtoSet(homework.getStudents()));
      toResponse.setTutor(tutorMapper.toNRDto(homework.getTutor()));
      return ResponseEntity.ok(toResponse);
    }
    else {
      HomeworkNRDto toResponse = homeworkMapper.toNRDto(homeworkService.getHomeworkById(id));
      return ResponseEntity.ok(toResponse);
    }
  }

  @GetMapping
  public ResponseEntity<List<HomeworkNRDto>> getAllHomeworks() {
    List<Homework> homeworks = homeworkService.getAllHomeworks();
    List<HomeworkNRDto> homeworkNRDtos = homeworkMapper.toNRDtos(homeworks);
    return ResponseEntity.ok(homeworkNRDtos);
  }

  @PutMapping("/{id}")
  public ResponseEntity<HomeworkRDto> updateHomework(@PathVariable Integer id, @RequestBody HomeworkRDto homeworkRDto) {
    Homework toUpdate = homeworkMapper.toNREntity(homeworkMapper.toNRDto(homeworkRDto));
    if (homeworkRDto.getTutor() != null) toUpdate.setTutor(tutorMapper.toNREntity(homeworkRDto.getTutor()));
    if (homeworkRDto.getTasks() != null) toUpdate.setTasks(homeworkTaskMapper.toEntitiesFromTDtos(homeworkRDto.getTasks()));
    if (homeworkRDto.getStudents() != null) toUpdate.setStudents(studentsHomeworkMapper.toEntitiesFromSDto(homeworkRDto.getStudents()));

    toUpdate = homeworkService.updateHomework(id, toUpdate);

    homeworkRDto = homeworkMapper.toRDto(homeworkMapper.toNRDto(toUpdate));
    homeworkRDto.setTasks(homeworkTaskMapper.toTDtos(toUpdate.getTasks()));
    homeworkRDto.setStudents(studentsHomeworkMapper.toSDtoSet(toUpdate.getStudents()));
    homeworkRDto.setTutor(tutorMapper.toNRDto(toUpdate.getTutor()));
    return ResponseEntity.ok(homeworkRDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteHomework(@PathVariable Integer id) {
    homeworkService.deleteHomework(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Добавляет новые задачи (HomeworkTask) в домашнее задание.
   * Принимает набор id задач в теле запроса.
   */
  @PostMapping("/{id}/tasks")
  public ResponseEntity<?> addTasksToHomework(@PathVariable Integer id,
                                                     @RequestBody Set<Integer> taskIds) {
    try {
      Homework updated = homeworkService.addTasksToHomework(id, taskIds);
      return ResponseEntity.ok(updated);
    }
    catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * Удаляет связи с задачами из домашнего задания.
   * Принимает список id задач, связи с которыми необходимо удалить.
   */
  @DeleteMapping("/{id}/tasks")
  public ResponseEntity<Homework> removeTasksFromHomework(@PathVariable Integer id,
                                                          @RequestBody List<Integer> taskIds) {
    Homework updated = homeworkService.removeTasksFromHomework(id, taskIds);
    return ResponseEntity.ok(updated);
  }
}
```

### controller\StudentController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.mapper.StudentMapper;
import com.example.easy_learning.mapper.StudentsHomeworkMapper;
import com.example.easy_learning.mapper.StudentsTutorsMapper;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;
  private final StudentMapper studentMapper;
  private final StudentsHomeworkMapper studentsHomeworkMapper;
  private final StudentsTutorsMapper studentsTutorsMapper;

  @PostMapping
  public ResponseEntity<StudentNRDto> createStudent(@RequestBody StudentNRDto studentNRDto) {
    Student toCreate = studentMapper.toNREntity(studentNRDto);
    StudentNRDto created = studentMapper.toNRDto(studentService.createStudent(toCreate));
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getStudent(@PathVariable Integer id,
                                      @RequestParam(required = false, defaultValue = "false") boolean full) {
    if (full) {
      Student student = studentService.getStudentById(id);
      StudentRDto studentRDto = studentMapper.toRDto(studentMapper.toNRDto(student));
      studentRDto.setHomeworks(studentsHomeworkMapper.toHDtoSet(student.getHomeworks()));
      studentRDto.setTutors(studentsTutorsMapper.toTDtoSet(student.getTutors()));
      studentRDto.setPassword(null);
      return ResponseEntity.ok(studentRDto);
    }
    StudentNRDto studentNRDto = studentMapper.toNRDto(studentService.getStudentById(id));
    studentNRDto.setPassword(null);
    return ResponseEntity.ok(studentNRDto);
  }

  @GetMapping
  public ResponseEntity<List<StudentNRDto>> getAllStudents() {
    List<Student> students = studentService.getAllStudents();
    List<StudentNRDto> studentNRDtos = studentMapper.toNRDtos(students)
            .stream().map(s -> {
              s.setPassword(null);
              return s;
            }).collect(Collectors.toList());
    return ResponseEntity.ok(studentNRDtos);
  }

  @PutMapping("/{id}")
  public ResponseEntity<StudentRDto> updateStudent(@PathVariable Integer id, @RequestBody StudentRDto studentRDto) {
    Student toUpdate = studentMapper.toNREntity(studentMapper.toNRDto(studentRDto));
    if (studentRDto.getHomeworks() != null) toUpdate.setHomeworks(studentsHomeworkMapper.toEntitiesFromHDto(studentRDto.getHomeworks()));
    if (studentRDto.getTutors() != null) toUpdate.setTutors(studentsTutorsMapper.toEntitiesFromTDto(studentRDto.getTutors()));

    Student updated = studentService.updateStudent(id, toUpdate);

    studentRDto = studentMapper.toRDto(studentMapper.toNRDto(updated));
    studentRDto.setTutors(studentsTutorsMapper.toTDtoSet(updated.getTutors()));
    studentRDto.setHomeworks(studentsHomeworkMapper.toHDtoSet(updated.getHomeworks()));
    return ResponseEntity.ok(studentRDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
    studentService.deleteStudent(id);
    return ResponseEntity.noContent().build();
  }
}
```

### controller\TaskController.java
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
```

### controller\TutorController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import com.example.easy_learning.mapper.*;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;
    private final TutorMapper tutorMapper;
    private final HomeworkMapper homeworkMapper;
    private final TaskMapper taskMapper;
    private final StudentsTutorsMapper studentsTutorsMapper;

    @PostMapping
    public ResponseEntity<TutorNRDto> create(@RequestBody TutorNRDto tutorNRDto) {
        Tutor tutor = tutorMapper.toNREntity(tutorNRDto);
        Tutor saved = tutorService.create(tutor);
        return ResponseEntity.ok(tutorMapper.toNRDto(saved));
    }
/*
    @PutMapping("/{id}")
    public ResponseEntity<TutorRDto> update(@PathVariable Integer id, @RequestBody TutorRDto tutorRDto) {
        Tutor updatedTutor = tutorMapper.toEntity(tutorRDto);

        if (tutorRDto.getTasks() != null) {
            updatedTutor.setTasks(taskMapper.toNREntities(tutorRDto.getTasks()));
        }
        if (tutorRDto.getHomeworks() != null) {
            updatedTutor.setHomeworks(homeworkMapper.toNREntities(tutorRDto.getHomeworks()));
        }
        if (tutorRDto.getStudents() != null) {
            updatedTutor.setStudents(studentsTutorsMapper.toEntitiesFromTDto(tutorRDto.getStudents()));
        }

        Tutor updated = tutorService.update(id, updatedTutor);
        TutorRDto responseDto = tutorMapper.toRDto(updated);


        TutorRDto responseDto = tutorMapper.toRDto(tutorMapper.toNRDto(updated));
        responseDto.setTasks(taskMapper.toNRDtos(updated.getTasks()));
        responseDto.setHomeworks(homeworkMapper.toNRDtos(updated.getHomeworks()));
        responseDto.setStudents(studentsTutorsMapper.toTDtoSet(updated.getStudents()));
        responseDto.setPassword(null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id,
                                     @RequestParam(defaultValue = "false") boolean full) {
        Tutor tutor = full ? tutorService.getByIdWithRelations(id) : tutorService.getById(id);
        if (full) {
            TutorRDto tutorRDto = tutorMapper.toRDto(tutorMapper.toNRDto(tutor));
            tutorRDto.setTasks(taskMapper.toNRDtos(tutor.getTasks()));
            tutorRDto.setHomeworks(homeworkMapper.toNRDtos(tutor.getHomeworks()));
            tutorRDto.setStudents(studentsTutorsMapper.toTDtoSet(tutor.getStudents()));
            tutorRDto.setPassword(null);
            return ResponseEntity.ok(tutorRDto);
        } else {
            TutorNRDto tutorNRDto = tutorMapper.toNRDto(tutor);
            tutorNRDto.setPassword(null);
            return ResponseEntity.ok(tutorNRDto);
        }
    }

    @GetMapping
    public ResponseEntity<List<TutorNRDto>> getAll() {
        List<TutorNRDto> dtos = tutorService.getAll().stream()
                .map(tutorMapper::toNRDto)
                .peek(t -> t.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }*/
}
```

### dto\auth\JwtRequest.java
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
    @NotBlank(message = "Username must not be blank")
    private String username;

    @Schema(description = "Password", example = "12345")
    @NotBlank(message = "Password must not be blank")
    private String password;
}
```

### dto\auth\JwtResponse.java
```java
package com.example.easy_learning.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response containing JWT tokens")
public class JwtResponse {
    private Long id;
    private String username;
    private String accessToken;
    private String refreshToken;
}
```

### dto\HomeworkNRDto.java
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

### dto\HomeworkRDto.java
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

### dto\HomeworkTaskHDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class HomeworkTaskHDto {
  private Integer id;
  private HomeworkNRDto homework;
}
```

### dto\HomeworkTaskTDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class HomeworkTaskTDto {

  private final Integer id;
  private final TaskNRDto task;
}
```

### dto\JwtResponse.java
```java
package com.example.easy_learning.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private Long id;
    private String username;
    private String accessToken;
    private String refreshToken;
}
```

### dto\RegisterDto.java
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

}
```

### dto\RegisterResponse.java
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

### dto\StudentNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class StudentNRDto {

  private Integer id;

  private String password;

  private StudentPersonalInfoDto studentPersonalInfo;
}

```

### dto\StudentPersonalInfoDto.java
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

  private String email;

  private String phone;

  private String telegram;
}
```

### dto\StudentRDto.java
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

  private Set<StudentsHomeworkHDto> homeworks;

  private Set<StudentsTutorsTDto> tutors;
}
```

### dto\StudentsHomeworkHDto.java
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

### dto\StudentsHomeworkSDto.java
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

### dto\StudentsTutorsSDto.java
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

### dto\StudentsTutorsTDto.java
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

### dto\TaskNRDto.java
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

### dto\TaskRDto.java
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

### dto\TutorNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class TutorNRDto {

  private Integer id;

  private TutorPersonalInfoDto personalInfo;
}
```

### dto\TutorPersonalInfoDto.java
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

  private String email;

  private String phone;

  private String telegram;
}
```

### dto\TutorRDto.java
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

  private Set<StudentNRDto> students;
}
```

### EasyLearningApplication.java
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

### mapper\HomeworkMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.model.Homework;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {

  Homework toNREntity(HomeworkNRDto homeworkNRDto);

  HomeworkNRDto toNRDto(Homework homework);

  HomeworkRDto toRDto(HomeworkNRDto homeworkNRDto);
  HomeworkNRDto toNRDto(HomeworkRDto homeworkRDto);

  List<Homework> toNREntities(List<HomeworkNRDto> homeworkNRDtos);

  List<HomeworkNRDto> toNRDtos(List<Homework> homeworks);
  List<HomeworkRDto> toRDtos(List<HomeworkNRDto> homeworkNRDtoss);
}
```

### mapper\HomeworkTaskMapper.java
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
  HomeworkTaskHDto toHDto(HomeworkTask homeworkTask);
  HomeworkTaskTDto toTDto(HomeworkTask homeworkTask);

  Set<HomeworkTaskHDto> toHDtos(Set<HomeworkTask> homeworkTasks);
  Set<HomeworkTaskTDto> toTDtos(Set<HomeworkTask> homeworkTasks);

  Set<HomeworkTask> toEntitiesFromHDtos(Set<HomeworkTaskHDto> homeworkTaskHDtos);
  Set<HomeworkTask> toEntitiesFromTDtos(Set<HomeworkTaskTDto> homeworkTaskTDtos);
}
```

### mapper\StudentMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.StudentPersonalInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StudentPersonalInfoMapper.class})
public interface StudentMapper {

  Student toNREntity(StudentNRDto studentNRDto);

  StudentNRDto toNRDto(Student student);

  StudentRDto toRDto(StudentNRDto studentNRDto);
  StudentNRDto toNRDto(StudentRDto studentRDto);

  List<Student> toNREntities(List<StudentNRDto> studentNRDtos);

  List<StudentNRDto> toNRDtos(List<Student> students);
  List<StudentRDto> toRDtos(List<StudentNRDto> studentNRDtoss);

  Student toEntity(RegisterDto registerDto);
}
```

### mapper\StudentPersonalInfoMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentPersonalInfoDto;
import com.example.easy_learning.model.StudentPersonalInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentPersonalInfoMapper {
  StudentPersonalInfoDto toDto(StudentPersonalInfo studentPersonalInfo);
  StudentPersonalInfo toEntity(StudentPersonalInfoDto studentPersonalInfoDto);
}
```

### mapper\StudentsHomeworkMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsHomeworkHDto;
import com.example.easy_learning.dto.StudentsHomeworkSDto;
import com.example.easy_learning.model.StudentsHomework;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, StudentMapper.class})
public interface StudentsHomeworkMapper {
  StudentsHomeworkHDto toHDto(StudentsHomework studentsHomework);
  StudentsHomeworkSDto toSDto(StudentsHomework studentsHomework);



  Set<StudentsHomeworkHDto> toHDtoSet(Set<StudentsHomework> studentsHomeworkSet);
  Set<StudentsHomeworkSDto> toSDtoSet(Set<StudentsHomework> studentsHomeworkSet);

  Set<StudentsHomework> toEntitiesFromSDto(Set<StudentsHomeworkSDto> studentsHomeworkSet);
  Set<StudentsHomework> toEntitiesFromHDto(Set<StudentsHomeworkHDto> studentsHomeworkSet);
}
```

### mapper\StudentsTutorsMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsTutorsSDto;
import com.example.easy_learning.dto.StudentsTutorsTDto;
import com.example.easy_learning.model.StudentsTutors;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, TutorMapper.class})
public interface StudentsTutorsMapper {
  StudentsTutorsSDto toSDto(StudentsTutors studentsTutors);
  StudentsTutorsTDto toTDto(StudentsTutors studentsTutors);

  Set<StudentsTutorsTDto> toTDtoSet(Set<StudentsTutors> studentsTutorsSet);
  Set<StudentsTutorsSDto> toSDtoSet(Set<StudentsTutors> studentsTutorsSet);

  Set<StudentsTutors> toEntitiesFromTDto(Set<StudentsTutorsTDto> studentsTutorsSet);
  Set<StudentsTutors> toEntitiesFromSDto(Set<StudentsTutorsSDto> studentsTutorsSet);
}
```

### mapper\TaskMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.dto.TaskRDto;
import com.example.easy_learning.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  @Mapping(target = "tutor.id", source = "tutorId")
  Task toNREntity(TaskNRDto taskNRDto);

  @Mapping(target = "tutorId", source = "tutor.id")
  TaskNRDto toNRDto(Task task);
  TaskRDto toRDto(TaskNRDto taskNRDto);

  Set<Task> toNREntities(Set<TaskNRDto> taskNRDtos);

  List<TaskNRDto> toNRDtos(List<Task> tasks);
  Set<TaskRDto> toRDtos(Set<TaskNRDto> taskNRDtoss);
}
```

### mapper\TutorMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import com.example.easy_learning.model.Tutor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TutorPersonalInfoMapper.class})
public interface TutorMapper {

  Tutor toNREntity(TutorNRDto tutorNRDto);
  TutorRDto toRDto(Tutor tutor); // <-- этот нужен в контроллере
  Tutor toEntity(TutorRDto tutorRDto); // <-- для обратного преобразования

  @Mapping(target = "personalInfo", source = "personalInfo")
  TutorNRDto toNRDto(Tutor tutor);
  TutorRDto toRDto(TutorNRDto tutorNRDto);

  Set<Tutor> toNREntities(Set<TutorNRDto> tutorNRDtos);

  Set<TutorNRDto> toNRDtos(Set<Tutor> tutors);
  Set<TutorRDto> toRDtos(Set<TutorNRDto> tutorNRDtoss);

  Tutor toTutor(RegisterDto registerDto);
}
```

### mapper\TutorPersonalInfoMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TutorPersonalInfoDto;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.model.TutorPersonalInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TutorPersonalInfoMapper {

  TutorPersonalInfoDto toTutorPersonalInfoDto(TutorPersonalInfo tutorPersonalInfo);
  TutorPersonalInfo toTutorPersonalInfo(TutorPersonalInfoDto tutorPersonalInfoDto);
}
```

### model\Homework.java
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

    @Column(name = "class", nullable = false)
    private String className; // Используем "className" вместо "class"

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

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

### model\HomeworkTask.java
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

### model\Student.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private StudentPersonalInfo studentPersonalInfo;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsHomework> homeworks = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsTutors> tutors = new HashSet<>();

    public void setHomeworks(Set<StudentsHomework> homeworks) {
        this.homeworks.clear();
        homeworks.forEach(h -> h.setStudent(this));
    }

    public void setTutors(Set<StudentsTutors> tutors) {
        this.tutors.clear();
        tutors.forEach(t -> t.setStudent(this));
    }
}
```

### model\StudentPersonalInfo.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.Column;
import lombok.*;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class StudentPersonalInfo {
  @Column(name = "firstname")
  private String firstname;

  @Column(name = "lastname")
  private String lastname;

  @Column(name = "birthdate")
  private LocalDate birthdate;

  @Column(name = "class")
  private String className; // Используем "className", так как "class" — зарезервированное слово

  @Column(name = "subject")
  private String subject;

  @Column(name = "phone", unique = true)
  private String phone;

  @Column(name = "telegram", unique = true)
  private String telegram;
}
```

### model\StudentsHomework.java
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
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;

    public void setStudent(Student student) {
        this.student = student;
        if (student != null && student.getHomeworks() != null) student.getHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null && homework.getStudents() != null) homework.getStudents().add(this);
    }
}
```

### model\StudentsTutors.java
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
  private Student student;

  @ManyToOne
  @JoinColumn(name = "tutor_id")
  private Tutor tutor;

  public void setStudent(Student student) {
    this.student = student;
    if (student != null && student.getTutors() != null) student.getTutors().add(this);
  }

  public void setTutor(Tutor tutor) {
    this.tutor = tutor;
    if (tutor != null && tutor.getStudents() != null) tutor.getStudents().add(this);
  }
}
```

### model\Task.java
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
    private Tutor tutor;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HomeworkTask> homeworks = new HashSet<>();
}
```

### model\Tutor.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tutor")
@Getter
@Setter
@NoArgsConstructor
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private TutorPersonalInfo personalInfo;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Homework> homeworks = new HashSet<>();

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsTutors> students = new HashSet<>();
}
```

### model\TutorPersonalInfo.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class TutorPersonalInfo {
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
}
```

### repository\HomeworkRepository.java
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

### repository\StudentRepository.java
```java
package com.example.easy_learning.repository;

import com.example.easy_learning.model.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

  @Query("SELECT s FROM Student s WHERE s.id = :id")
  @EntityGraph(attributePaths = {
          "homeworks",
          "homeworks.homework",
          "homeworks.homework.tasks",
          "tutors",
          "tutors.tutor"
  })
  Optional<Student> findStudentWithAssociationsById(@Param("id") Integer id);
  Optional<Student> findByEmail(String email);

}
```

### repository\TaskRepository.java
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

### repository\TutorRepository.java
```java
package com.example.easy_learning.repository;

import com.example.easy_learning.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {

    Optional<Tutor> findByEmail(String email);

}

```

### security\JwtTokenFilter.java
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
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String bearerToken = ((HttpServletRequest) servletRequest).getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken = bearerToken.substring(7);
        }

        try {
            if (bearerToken != null && jwtTokenProvider.isValid(bearerToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(bearerToken);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
```

### security\JwtTokenProvider.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.dto.JwtResponse;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.StudentService;
import com.example.easy_learning.service.TutorService;
import com.example.easy_learning.service.props.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final StudentJwtUserDetailsService studentJwtUserDetailsService;
     private final TutorJwtUserDetailsService tutorJwtUserDetailsService;
    private final StudentService studentService;
    private final TutorService tutorService;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String createAccessToken(Long userId, String username, String userType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccess(), ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .claim("userType", userType)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, String username, String userType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefresh(), ChronoUnit.DAYS);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .claim("userType", userType)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtResponse refreshStudentTokens(String refreshToken) {
        if (!isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        Long studentId = Long.valueOf(getId(refreshToken));
        Student student = studentService.getStudentById(studentId.intValue());

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(student.getId().longValue());
        jwtResponse.setUsername(student.getEmail());
        jwtResponse.setAccessToken(createAccessToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        jwtResponse.setRefreshToken(createRefreshToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        return jwtResponse;
    }

    public JwtResponse refreshTutorTokens(String refreshToken) {
        if (!isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        Long tutorId = Long.valueOf(getId(refreshToken));
        Tutor tutor = tutorService.getById(tutorId.intValue());

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(tutor.getId().longValue());
        jwtResponse.setUsername(tutor.getEmail());
        jwtResponse.setAccessToken(createAccessToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        jwtResponse.setRefreshToken(createRefreshToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        return jwtResponse;
    }

    public boolean isValid(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return claimsJws.getBody().getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String getId(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", String.class);
    }

    private String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUserType(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userType", String.class);
    }


    public Authentication getAuthentication(String token) {
        String userType = getUserType(token);

        if ("student".equals(userType)) {
            return getAuthenticationForStudent(token);
        } else if ("tutor".equals(userType)) {
            return getAuthenticationForTutor(token);
        } else {
            return null;
        }
    }

    public Authentication getAuthenticationForStudent(String token) {
        String username = getUsername(token);
        UserDetails userDetails = studentJwtUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Authentication getAuthenticationForTutor(String token) {
        String username = getUsername(token);
        UserDetails userDetails = tutorJwtUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
```

### security\StudentJwtEntity.java
```java
package com.example.easy_learning.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class StudentJwtEntity implements UserDetails {

    private final Integer id;
    private final String username; // email
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // если ролей нет
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

### security\StudentJwtEntityFactory.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.model.Student;

import java.util.Collections;

public final class StudentJwtEntityFactory {

    public static StudentJwtEntity create(Student student) {
        return new StudentJwtEntity(
                student.getId(),
                student.getEmail(),
                student.getPassword()
        );
    }
}
```

### security\StudentJwtUserDetailsService.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.model.Student;
import com.example.easy_learning.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentJwtUserDetailsService implements UserDetailsService {

    private final StudentService studentService;

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        Student student = studentService.getByEmail(email);
        return StudentJwtEntityFactory.create(student);
    }
}
```

### security\TutorJwtEntity.java
```java
package com.example.easy_learning.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class TutorJwtEntity implements UserDetails {

    private final Integer id;
    private final String username; // email
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // если ролей нет
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

### security\TutorJwtEntityFactory.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.model.Tutor;

import java.util.Collections;

public final class TutorJwtEntityFactory {

    public static TutorJwtEntity create(Tutor tutor) {
        return new TutorJwtEntity(
                tutor.getId(),
                tutor.getEmail(),
                tutor.getPassword()
        );
    }
}
```

### security\TutorJwtUserDetailsService.java
```java
package com.example.easy_learning.security;

import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorJwtUserDetailsService implements UserDetailsService {

    private final TutorService tutorService;

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        Tutor tutor = tutorService.getByEmail(email);
        return TutorJwtEntityFactory.create(tutor);
    }
}
```

### service\AuthService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.dto.JwtResponse;
import org.springframework.stereotype.Service;


@Service
public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(String refreshToken);
}
```

### service\HomeworkService.java
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

### service\impl\AuthServiceImpl.java
```java
package com.example.easy_learning.service.impl;

import com.example.easy_learning.dto.JwtResponse;
import com.example.easy_learning.dto.auth.JwtRequest;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.security.JwtTokenProvider;
import com.example.easy_learning.service.AuthService;
import com.example.easy_learning.service.StudentService;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final StudentService studentService;
    private final TutorService tutorService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        // Аутентификация
        var auth = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()
                )
        );

        // Попробуем сначала как студент
        try {
            Student student = studentService.getByEmail(loginRequest.getUsername());
            return buildStudentTokens(student);
        } catch (RuntimeException e) {
            // Если студент не найден — пробуем репетитора
            Tutor tutor = tutorService.getByEmail(loginRequest.getUsername());
            return buildTutorTokens(tutor);
        }
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        String userType = jwtTokenProvider.getUserType(refreshToken);
        if ("student".equals(userType)) {
            return jwtTokenProvider.refreshStudentTokens(refreshToken);
        } else if ("tutor".equals(userType)) {
            return jwtTokenProvider.refreshTutorTokens(refreshToken);
        } else {
            throw new RuntimeException("Invalid user type");
        }
    }

    private JwtResponse buildStudentTokens(Student student) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(student.getId().longValue());
        jwtResponse.setUsername(student.getEmail());
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        return jwtResponse;
    }

    private JwtResponse buildTutorTokens(Tutor tutor) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(tutor.getId().longValue());
        jwtResponse.setUsername(tutor.getEmail());
        jwtResponse.setAccessToken(jwtTokenProvider.createAccessToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        jwtResponse.setRefreshToken(jwtTokenProvider.createRefreshToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        return jwtResponse;
    }
}
```

### service\impl\HomeworkServiceImpl.java
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

### service\impl\StudentServiceImpl.java
```java
package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Student;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.StudentRepository;
import com.example.easy_learning.service.StudentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final HomeworkRepository homeworkRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Student createStudent(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        return studentRepository.save(student);
    }

    @Override
    public Student getStudentById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
    }

    @Override
    public Student getStudentByIdWithAllRelations(Integer id) {
        return studentRepository.findStudentWithAssociationsById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
    }

    @Override
    public Student updateStudent(Integer id, Student updatedStudent) {
        Student existingStudent = getStudentByIdWithAllRelations(id);
        if (updatedStudent.getHomeworks() != null) {
            existingStudent.setHomeworks(updatedStudent.getHomeworks());
        }

        if (updatedStudent.getTutors() != null) {
            existingStudent.setTutors(updatedStudent.getTutors());
        }

        if (!existingStudent.getPassword().equals(updatedStudent.getPassword())) {
            existingStudent.setPassword(passwordEncoder.encode(updatedStudent.getPassword()));
        }
        if (updatedStudent.getStudentPersonalInfo() != null) {
            existingStudent.setStudentPersonalInfo(updatedStudent.getStudentPersonalInfo());
        }
        return studentRepository.save(existingStudent);
    }

    @Override
    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Студент с email " + email + " не найден"));
    }
}
```

### service\impl\TaskServiceImpl.java
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

### service\impl\TutorServiceImpl.java
```java
package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.repository.TutorRepository;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Tutor create(Tutor tutor) {
        tutor.setPassword(passwordEncoder.encode(tutor.getPassword()));
        return tutorRepository.save(tutor);
    }

    @Override
    @Transactional
    public Tutor update(Integer id, Tutor updatedTutor) {
        Tutor existing = getById(id);

        existing.setPersonalInfo(updatedTutor.getPersonalInfo());

        if (!existing.getPassword().equals(updatedTutor.getPassword())) {
            existing.setPassword(passwordEncoder.encode(updatedTutor.getPassword()));
        }

        if (updatedTutor.getTasks() != null) {
            existing.setTasks(updatedTutor.getTasks());
        }

        if (updatedTutor.getHomeworks() != null) {
            existing.setHomeworks(updatedTutor.getHomeworks());
        }

        if (updatedTutor.getStudents() != null) {
            existing.setStudents(updatedTutor.getStudents());
        }

        return tutorRepository.save(existing);
    }

    @Override
    public Tutor getById(Integer id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor not found with id: " + id));
    }

    @Override
    public Tutor getByEmail(String email) {
        return tutorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Тьютор с email " + email + " не найден"));
    }


    @Override
    public Tutor getByIdWithRelations(Integer id) {
        // Пока нет кастомного запроса с @EntityGraph — возвращаем обычный getById
        return getById(id);
    }

    @Override
    public List<Tutor> getAll() {
        return tutorRepository.findAll();
    }
}
```

### service\props\JwtProperties.java
```java
package com.example.easy_learning.service.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    private long access;
    private long refresh;
}
```

### service\StudentService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Student;

import java.util.List;

public interface StudentService {
  Student createStudent(Student student);
  Student getStudentById(Integer id);
  Student getStudentByIdWithAllRelations(Integer id);
  Student updateStudent(Integer id, Student updatedStudent);
  void deleteStudent(Integer id);
  List<Student> getAllStudents();
  Student getByEmail(String email);
}
```

### service\TaskService.java
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

### service\TutorService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Tutor;

import java.util.List;

public interface TutorService {

  Tutor create(Tutor tutor);

  Tutor update(Integer id, Tutor updatedTutor);

  Tutor getById(Integer id);

  Tutor getByEmail(String email);


  Tutor getByIdWithRelations(Integer id);

  List<Tutor> getAll();
}
```

