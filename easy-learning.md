### app\uploads\7607267c-b245-4764-bd35-0ddd99d5e63f.jpg
```
Ошибка чтения файла: Input length = 1
```

### app\uploads\7a2be1e3-c44e-4ddd-88c6-618cc549cb31.jpg
```
Ошибка чтения файла: Input length = 1
```

### app\uploads\87006426-8553-4735-b3d0-9700f5883cf7.jpg
```
Ошибка чтения файла: Input length = 1
```

### app\uploads\9c23e31f-7f05-42de-a82d-b4f063c54eff.jpg
```
Ошибка чтения файла: Input length = 1
```

### app\uploads\c4429b71-4ccf-4595-be4c-b902d50ceee4.jpg
```
Ошибка чтения файла: Input length = 1
```

### docker-compose.yml
```yaml
services:
  app:
    build: .
    image: easy_learning_app
    container_name: easy_learning_app
    ports:
      - "8080:8080"
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    container_name: mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: 12345
      MYSQL_DATABASE: easy_learning
    ports:
      - "2020:3306"
    volumes:
      - mysql_data:/var/lib/mysql


volumes:
  mysql_data:
```

### Dockerfile
```dockerfile
# Stage 1: сборка приложения с помощью Maven
FROM maven:3-openjdk-17 AS builder
WORKDIR /app
# Копируем файлы pom.xml и src, чтобы затем выполнить сборку
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Сборка приложения, пропуская тесты
RUN mvn clean package -DskipTests

# Stage 2: запуск приложения на легковесном образе OpenJDK
FROM openjdk:17-jdk-slim
WORKDIR /app
# Копируем скомпилированный jar из стадии сборки
COPY --from=builder /app/target/easy_learning-0.0.1-SNAPSHOT.jar app.jar

# Загружаем скрипт wait-for-it и делаем его исполняемым
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8080
# Ожидаем, пока база MySQL станет доступной, затем запускаем приложение
ENTRYPOINT ["/wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]

```

### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.example</groupId>
    <artifactId>easy_learning</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>easy_learning</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.6.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>

        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### src\main\java\com\example\easy_learning\config\PasswordEncoderConfig.java
```java
package com.example.easy_learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class PasswordEncoderConfig {

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable) // отключаем CSRF, если запросы приходят не из браузера
      .authorizeHttpRequests(authorize -> authorize
          .anyRequest().permitAll()
      );

    return http.build();
  }
}
```

### src\main\java\com\example\easy_learning\controller\HomeworkController.java
```java
package com.example.easy_learning.controller;

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

  @PostMapping
  public ResponseEntity<Homework> createHomework(@RequestBody Homework homework) {
    Homework created = homeworkService.createHomework(homework);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Homework> getHomework(@PathVariable Integer id,
                                              @RequestParam(value = "full", defaultValue = "false") boolean full) {
    Homework homework = full
            ? homeworkService.getHomeworkWithAssociationsById(id)
            : homeworkService.getHomeworkById(id);
    return ResponseEntity.ok(homework);
  }

  @GetMapping
  public ResponseEntity<List<Homework>> getAllHomeworks() {
    List<Homework> homeworks = homeworkService.getAllHomeworks();
    return ResponseEntity.ok(homeworks);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Homework> updateHomework(@PathVariable Integer id, @RequestBody Homework homework) {
    Homework updated = homeworkService.updateHomework(id, homework);
    return ResponseEntity.ok(updated);
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

### src\main\java\com\example\easy_learning\controller\StudentController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.model.Student;
import com.example.easy_learning.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @PostMapping
  public ResponseEntity<Student> createStudent(@RequestBody Student student) {
    Student created = studentService.createStudent(student);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Student> getStudent(@PathVariable Integer id,
                                            @RequestParam(required = false, defaultValue = "false") boolean full) {
    Student student = full
            ? studentService.getStudentByIdWithAllRelations(id)
            : studentService.getStudentById(id);
    return ResponseEntity.ok(student);
  }

  @GetMapping
  public ResponseEntity<List<Student>> getAllStudents() {
    List<Student> students = studentService.getAllStudents();
    return ResponseEntity.ok(students);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Student> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
    Student updated = studentService.updateStudent(id, student);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStudent(@PathVariable Integer id) {
    studentService.deleteStudent(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Добавляет новые домашние задания для студента.
   * Принимает набор id домашних заданий и возвращает обновленного студента.
   */
  @PostMapping("/{id}/homeworks")
  public ResponseEntity<?> addHomeworksToStudent(@PathVariable Integer id,
                                                 @RequestBody Set<Integer> homeworkIds) {
    try {
      Student updated = studentService.addHomeworksToStudent(id, homeworkIds);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * Удаляет связи с домашними заданиями у студента.
   * Принимает список id домашних заданий, которые необходимо убрать.
   */
  @DeleteMapping("/{id}/homeworks")
  public ResponseEntity<Student> removeHomeworksFromStudent(@PathVariable Integer id,
                                                            @RequestBody List<Integer> homeworkIds) {
    Student updated = studentService.removeHomeworksFromStudent(id, homeworkIds);
    return ResponseEntity.ok(updated);
  }
}
```

### src\main\java\com\example\easy_learning\controller\TaskController.java
```java
package com.example.easy_learning.controller;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.mapper.HomeworkTaskMapper;
import com.example.easy_learning.mapper.TaskMapper;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;
  private final HomeworkTaskMapper homeworkTaskMapper;

  /**
   * Создание новой задачи без файла.
   */
  @PostMapping(consumes = "application/json")
  public ResponseEntity<Task> createTask(@RequestBody TaskNRDto taskNRDto) {
    Task toCreate = taskMapper.toNREntity(taskNRDto);
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
    Task created = taskService.createTaskWithFile(toCreate, file);
    return ResponseEntity.ok(created);
  }

  /**
   * Получение задачи по id.
   * Если параметр full=true, возвращаются все связи (используется метод getTaskByIdWithAllRelations).
   */
  @GetMapping("/{id}")
  public ResponseEntity<Task> getTask(@PathVariable Integer id,
                                      @RequestParam(value = "full", defaultValue = "false") boolean full) {
    Task task = full ? taskService.getTaskByIdWithAllRelations(id) : taskService.getTaskById(id);
    return ResponseEntity.ok(task);
  }

  /**
   * Получение списка всех задач.
   */
  @GetMapping
  public ResponseEntity<List<Task>> getAllTasks() {
    List<Task> tasks = taskService.getAllTasks();
    return ResponseEntity.ok(tasks);
  }

  /**
   * Обновление задачи. Если передан новый файл, он будет обработан.
   * JSON-часть запроса передаётся в поле "task", файл – в поле "file" (необязательный).
   */
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Task> updateTask(@PathVariable Integer id,
                                         @RequestPart("task") Task task,
                                         @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
    Task updated = taskService.updateTask(id, task, file);
    return ResponseEntity.ok(updated);
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

### src\main\java\com\example\easy_learning\dto\HomeworkNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

import java.util.Set;

@Data
public class HomeworkNRDto {

  private Integer id;

  private String className; // Используем "className" вместо "class"

  private String subject;

  private String topic;

  private Integer difficulty;
}
```

### src\main\java\com\example\easy_learning\dto\HomeworkRDto.java
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

### src\main\java\com\example\easy_learning\dto\HomeworkTaskHDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class HomeworkTaskHDto {
  private Integer id;
  private HomeworkNRDto homework;
}
```

### src\main\java\com\example\easy_learning\dto\HomeworkTaskTDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class HomeworkTaskTDto {

  private final Integer id;
  private final TaskNRDto taskNrDto;
}
```

### src\main\java\com\example\easy_learning\dto\StudentNRDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

@Data
public class StudentNRDto {

  private Integer id;

  private StudentPersonalInfoDto studentPersonalInfo;

  private String password;
}
```

### src\main\java\com\example\easy_learning\dto\StudentPersonalInfoDto.java
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

### src\main\java\com\example\easy_learning\dto\StudentRDto.java
```java
package com.example.easy_learning.dto;

import com.example.easy_learning.model.StudentsHomework;
import lombok.Data;

import java.util.Set;

@Data
public class StudentRDto {

  private Integer id;

  private StudentPersonalInfoDto studentPersonalInfo;

  private Set<StudentsHomeworkHDto> homeworks;

  private Set<TutorNRDto> tutors;
}
```

### src\main\java\com\example\easy_learning\dto\StudentsHomeworkHDto.java
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

### src\main\java\com\example\easy_learning\dto\StudentsHomeworkSDto.java
```java
package com.example.easy_learning.dto;

import lombok.Data;

public class StudentsHomeworkSDto {
    private Integer id;

    private StudentNRDto student;

    private Boolean isDone;

    private Boolean isChecked;

    private Integer score;
}
```

### src\main\java\com\example\easy_learning\dto\TaskNRDto.java
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
}
```

### src\main\java\com\example\easy_learning\dto\TaskRDto.java
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

### src\main\java\com\example\easy_learning\dto\TutorNRDto.java
```java
package com.example.easy_learning.dto;

public class TutorNRDto {

  private Integer id;

  private TutorPersonalInfoDto personalInfo;
}
```

### src\main\java\com\example\easy_learning\dto\TutorPersonalInfoDto.java
```java
package com.example.easy_learning.dto;

import java.time.LocalDate;

public class TutorPersonalInfoDto {

  private String firstname;

  private String lastname;

  private LocalDate birthdate;

  private String email;

  private String phone;

  private String telegram;
}
```

### src\main\java\com\example\easy_learning\dto\TutorRDto.java
```java
package com.example.easy_learning.dto;

import java.util.Set;

public class TutorRDto {
  private Integer id;

  private TutorPersonalInfoDto personalInfo;

  private String password;

  private Set<TaskNRDto> tasks;

  private Set<HomeworkNRDto> homeworks;

  private Set<StudentNRDto> students;
}
```

### src\main\java\com\example\easy_learning\EasyLearningApplication.java
```java
package com.example.easy_learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EasyLearningApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasyLearningApplication.class, args);
	}

}
```

### src\main\java\com\example\easy_learning\mapper\HomeworkMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.model.Homework;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {

  Homework toNREntity(HomeworkNRDto homeworkNRDto);

  HomeworkNRDto toNRDto(Homework homework);
  HomeworkRDto toRDto(HomeworkNRDto homeworkNRDto);

  Set<Homework> toNREntities(Set<HomeworkNRDto> homeworkNRDtos);

  Set<HomeworkNRDto> toNRDtos(Set<Homework> homeworks);
  Set<HomeworkRDto> toRDtos(Set<HomeworkNRDto> homeworkNRDtoss);
}
```

### src\main\java\com\example\easy_learning\mapper\HomeworkTaskMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkTaskHDto;
import com.example.easy_learning.dto.HomeworkTaskTDto;
import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, TaskMapper.class})
public interface HomeworkTaskMapper {
  HomeworkTaskHDto toHDto(HomeworkTask homeworkTask);
  HomeworkTaskTDto toTDto(HomeworkTask homeworkTask);
}
```

### src\main\java\com\example\easy_learning\mapper\StudentMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.model.Student;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface StudentMapper {

  Student toNREntity(StudentNRDto studentNRDto);

  StudentNRDto toNRDto(Student student);
  StudentRDto toRDto(StudentNRDto studentNRDto);

  Set<Student> toNREntities(Set<StudentNRDto> studentNRDtos);

  Set<StudentNRDto> toNRDtos(Set<Student> students);
  Set<StudentRDto> toRDtos(Set<StudentNRDto> studentNRDtoss);
}
```

### src\main\java\com\example\easy_learning\mapper\StudentsHomework.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsHomeworkHDto;
import com.example.easy_learning.dto.StudentsHomeworkSDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, StudentMapper.class})
public interface StudentsHomework {
  StudentsHomeworkHDto toHDto(StudentsHomework studentsHomework);
  StudentsHomeworkSDto toSDto(StudentsHomework studentsHomework);
}
```

### src\main\java\com\example\easy_learning\mapper\TaskMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.dto.TaskRDto;
import com.example.easy_learning.model.Task;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  Task toNREntity(TaskNRDto taskNRDto);

  TaskNRDto toNRDto(Task task);
  TaskRDto toRDto(TaskNRDto taskNRDto);

  Set<Task> toNREntities(Set<TaskNRDto> taskNRDtos);

  Set<TaskNRDto> toNRDtos(Set<Task> tasks);
  Set<TaskRDto> toRDtos(Set<TaskNRDto> taskNRDtoss);
}
```

### src\main\java\com\example\easy_learning\mapper\TutorMapper.java
```java
package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import com.example.easy_learning.model.Tutor;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface TutorMapper {

  Tutor toNREntity(TutorNRDto tutorNRDto);

  TutorNRDto toNRDto(Tutor tutor);
  TutorRDto toRDto(TutorNRDto tutorNRDto);

  Set<Tutor> toNREntities(Set<TutorNRDto> tutorNRDtos);

  Set<TutorNRDto> toNRDtos(Set<Tutor> tutors);
  Set<TutorRDto> toRDtos(Set<TutorNRDto> tutorNRDtoss);
}
```

### src\main\java\com\example\easy_learning\model\Homework.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "homework")
@Data
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
    private Set<StudentsHomework> students;

    @OneToMany(mappedBy = "homework", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HomeworkTask> tasks;
}
```

### src\main\java\com\example\easy_learning\model\HomeworkTask.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "homework_task")
@Data
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
        if (task != null) task.getHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null) homework.getTasks().add(this);
    }
}
```

### src\main\java\com\example\easy_learning\model\Student.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "student")
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private StudentPersonalInfo studentPersonalInfo;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsHomework> homeworks;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsTutors> tutors;
}
```

### src\main\java\com\example\easy_learning\model\StudentPersonalInfo.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;

@Embeddable
@Data
public class StudentPersonalInfo {
  @Column(name = "firstname", nullable = false)
  private String firstname;

  @Column(name = "lastname", nullable = false)
  private String lastname;

  @Column(name = "birthdate", nullable = false)
  private LocalDate birthdate;

  @Column(name = "class", nullable = false)
  private String className; // Используем "className", так как "class" — зарезервированное слово

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "phone", nullable = false, unique = true)
  private String phone;

  @Column(name = "telegram", unique = true)
  private String telegram;
}
```

### src\main\java\com\example\easy_learning\model\StudentsHomework.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "students_homework")
@Data
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
        if (student != null) student.getHomeworks().add(this);
    }

    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null) homework.getStudents().add(this);
    }
}
```

### src\main\java\com\example\easy_learning\model\StudentsTutors.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "students_tutors")
@Data
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
}
```

### src\main\java\com\example\easy_learning\model\Task.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task")
@Data
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

### src\main\java\com\example\easy_learning\model\Tutor.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "tutor")
@Data
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private TutorPersonalInfo personalInfo;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks;

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Homework> homeworks;

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsTutors> students;
}
```

### src\main\java\com\example\easy_learning\model\TutorPersonalInfo.java
```java
package com.example.easy_learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDate;

@Embeddable
@Data
public class TutorPersonalInfo {
    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "telegram", unique = true)
    private String telegram;
}
```

### src\main\java\com\example\easy_learning\repository\HomeworkRepository.java
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

### src\main\java\com\example\easy_learning\repository\StudentRepository.java
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
}
```

### src\main\java\com\example\easy_learning\repository\TaskRepository.java
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

  @EntityGraph(attributePaths = {"homeworks"})
  @Query("SELECT t FROM Task t WHERE t.id = :id")
  Optional<Task> findByIdWithAllRelations(@Param("id") Integer id);
}
```

### src\main\java\com\example\easy_learning\repository\TutorRepository.java
```java
package com.example.easy_learning.repository;

import com.example.easy_learning.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {}
```

### src\main\java\com\example\easy_learning\service\HomeworkService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HomeworkService {

  private final HomeworkRepository homeworkRepository;
  private final TaskRepository taskRepository;

  public Homework createHomework(Homework homework) {
    return homeworkRepository.save(homework);
  }

  public Homework getHomeworkById(Integer id) {
    return homeworkRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Homework not found with id: " + id));
  }

  public Homework getHomeworkWithAssociationsById(Integer id) {
    return homeworkRepository.findHomeworkWithAssociationsById(id)
            .orElseThrow(() -> new RuntimeException("Homework not found with id: " + id));
  }

  public Homework updateHomework(Integer id, Homework updatedHomework) {
    Homework existing = getHomeworkById(id);
    existing.setClassName(updatedHomework.getClassName());
    existing.setSubject(updatedHomework.getSubject());
    existing.setTopic(updatedHomework.getTopic());
    existing.setDifficulty(updatedHomework.getDifficulty());
    existing.setTutor(updatedHomework.getTutor());
    return homeworkRepository.save(existing);
  }

  public void deleteHomework(Integer id) {
    homeworkRepository.deleteById(id);
  }

  public List<Homework> getAllHomeworks() {
    return homeworkRepository.findAll();
  }

  /**
   * Добавляет новые задачи (HomeworkTask) в домашнее задание.
   * Если хотя бы один из переданных taskId уже присутствует в Homework,
   * метод бросает исключение.
   *
   * @param homeworkId идентификатор домашнего задания
   * @param taskIds набор id задач для добавления
   * @return обновлённое домашнее задание с добавленными связями
   */
  @Transactional
  public Homework addTasksToHomework(Integer homeworkId, Set<Integer> taskIds) {
    Homework homework = getHomeworkWithAssociationsById(homeworkId);

    // Проверяем пересечение: если для какого-либо taskId уже существует связь, бросаем исключение
    for (Integer taskId : taskIds) {
      boolean exists = homework.getTasks().stream()
              .anyMatch(ht -> ht.getTask().getId().equals(taskId));
      if (exists) {
        throw new RuntimeException("Task with id " + taskId + " already exists in Homework");
      }
    }

    // Для каждого нового id создаём связь HomeworkTask
    for (Integer taskId : taskIds) {
      Task task = taskRepository.findById(taskId)
              .orElseThrow(() -> new RuntimeException("Task with id " + taskId + " not found"));
      HomeworkTask ht = new HomeworkTask();
      // Используем сеттеры, которые автоматически добавляют связь в коллекции
      ht.setTask(task);
      ht.setHomework(homework);
    }

    return homeworkRepository.save(homework);
  }

  /**
   * Удаляет из домашнего задания связи с задачами.
   * Для каждого объекта HomeworkTask, у которого task имеет id, входящий в taskIds,
   * вызывается setHomework(null) (для orphanRemoval) и объект удаляется из коллекции.
   *
   * @param homeworkId идентификатор домашнего задания
   * @param taskIds список id задач, связи с которыми необходимо удалить
   * @return обновлённое домашнее задание без указанных связей
   */
  @Transactional
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
}
```

### src\main\java\com\example\easy_learning\service\StudentService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.StudentsHomework;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final HomeworkRepository homeworkRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public Student createStudent(Student student) {
    student.setPassword(passwordEncoder.encode(student.getPassword()));
    return studentRepository.save(student);
  }

  public Student getStudentById(Integer id) {
    return studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
  }

  public Student getStudentByIdWithAllRelations(Integer id) {
    return studentRepository.findStudentWithAssociationsById(id)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + id));
  }

  public Student updateStudent(Integer id, Student updatedStudent) {
    Student existingStudent = getStudentById(id);

    if (!existingStudent.getPassword().equals(updatedStudent.getPassword())) {
      existingStudent.setPassword(passwordEncoder.encode(updatedStudent.getPassword()));
    }
    existingStudent.setStudentPersonalInfo(updatedStudent.getStudentPersonalInfo());
    return studentRepository.save(existingStudent);
  }

  public void deleteStudent(Integer id) {
    studentRepository.deleteById(id);
  }

  public List<Student> getAllStudents() {
    return studentRepository.findAll();
  }

  /**
   * Добавляет новые домашние задания (StudentsHomework) для студента.
   * Если хотя бы один из переданных id уже присутствует у студента,
   * метод бросает RuntimeException.
   *
   * @param studentId   идентификатор студента
   * @param homeworkIds набор id домашних заданий для добавления
   * @return обновленный объект Student с новыми связями
   */
  @Transactional
  public Student addHomeworksToStudent(Integer studentId, Set<Integer> homeworkIds) {
    // Получаем студента со всеми связями (используя метод с @EntityGraph)
    Student student = studentRepository.findStudentWithAssociationsById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + studentId));

    // Проверяем, что ни один из новых id не пересекается с уже существующими
    for (Integer hwId : homeworkIds) {
      boolean exists = student.getHomeworks().stream()
              .anyMatch(sh -> sh.getHomework().getId().equals(hwId));
      if (exists) {
        throw new RuntimeException("Homework with id " + hwId + " already exists for the student");
      }
    }

    // Для каждого нового id получаем Homework и создаем связь StudentsHomework
    for (Integer hwId : homeworkIds) {
      Homework homework = homeworkRepository.findById(hwId)
              .orElseThrow(() -> new RuntimeException("Homework with id " + hwId + " not found"));
      StudentsHomework sh = new StudentsHomework();
      sh.setHomework(homework);
      sh.setStudent(student);
    }

    return studentRepository.save(student);
  }

  /**
   * Удаляет из студента связи с домашними заданиями, идентификаторы которых переданы в homeworkIds.
   * Для каждой найденной связи вызывается setter с null для student, что при orphanRemoval
   * приводит к удалению соответствующей записи из БД.
   *
   * @param studentId   идентификатор студента
   * @param homeworkIds список id домашних заданий, связи с которыми необходимо убрать
   * @return обновленный объект Student без указанных связей
   */
  @Transactional
  public Student removeHomeworksFromStudent(Integer studentId, List<Integer> homeworkIds) {
    // Получаем студента со всеми связями
    Student student = studentRepository.findStudentWithAssociationsById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with ID = " + studentId));

    // Удаляем связи, если id домашек содержатся в homeworkIds.
    // Используем removeIf, и для каждой связи вызываем setter с null для student.
    student.getHomeworks().removeIf(sh -> {
      if (homeworkIds.contains(sh.getHomework().getId())) {
        sh.setStudent(null);
        return true;
      }
      return false;
    });

    return studentRepository.save(student);
  }
}
```

### src\main\java\com\example\easy_learning\service\TaskService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.TaskRepository;
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
public class TaskService {

  private final TaskRepository taskRepository;

  // Путь к папке, где храним все загруженные файлы
  private final String uploadDir = "/app/uploads";

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
    return taskRepository.findByIdWithAllRelations(id)
            .orElseThrow(() -> new RuntimeException("Task not found with ID = " + id));
  }

  /**
   * Обновить Task, в том числе, если нужен перезагрузить новый файл.
   */
  public Task updateTask(Integer id, Task updatedTask, MultipartFile file) throws IOException {
    Task existingTask = getTaskById(id);

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

    return taskRepository.save(existingTask);
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
    Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
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
    if (!Files.exists(path)) {
      throw new RuntimeException("Photo file not found for task with ID = " + taskId);
    }
    return Files.readAllBytes(path);
  }
}
```

### src\main\java\com\example\easy_learning\service\TutorService.java
```java
package com.example.easy_learning.service;

import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorService {

  private final TutorRepository tutorRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public Tutor createTutor(Tutor tutor) {
    tutor.setPassword(passwordEncoder.encode(tutor.getPassword()));
    return tutorRepository.save(tutor);
  }

  public Tutor getTutorById(Integer id) {
    return tutorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tutor not found with ID = " + id));
  }

  public Tutor updateTutor(Integer id, Tutor updatedTutor) {
    Tutor existingTutor = getTutorById(id);
    // При изменении пароля – хэшируем заново
    if (!existingTutor.getPassword().equals(updatedTutor.getPassword())) {
      existingTutor.setPassword(passwordEncoder.encode(updatedTutor.getPassword()));
    }
    existingTutor.setPersonalInfo(updatedTutor.getPersonalInfo());
    // при необходимости обновляем другие поля
    return tutorRepository.save(existingTutor);
  }

  public void deleteTutor(Integer id) {
    tutorRepository.deleteById(id);
  }

  public List<Tutor> getAllTutors() {
    return tutorRepository.findAll();
  }
}
```

### src\main\resources\application.yml
```yaml
spring:
  datasource:
#    url: jdbc:mysql://mysql:3306/easy_learning
    url: jdbc:mysql://localhost:1212/easy_learning
    username: root
    password: 12345
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: validate
  application:
    name: easy_learning
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
  security:
    user:
      name: admin
      password: $2a$12$HXd78PaHX.RUjzLhRTaLquTW2HzskPCm7Doq3RUXBsiQ0wCgT3iZW
```

### src\main\resources\db\changelog\db.changelog-master.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <!-- Подключаем основной файл изменений для версии v1 -->
    <include file="v1/db.changelog-v1.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

### src\main\resources\db\changelog\v1\db.changelog-v1.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <include file="tutor.xml" relativeToChangelogFile="true"/>
    <include file="homework.xml" relativeToChangelogFile="true"/>
    <include file="task.xml" relativeToChangelogFile="true"/>
    <include file="homework_task.xml" relativeToChangelogFile="true"/>
    <include file="student.xml" relativeToChangelogFile="true"/>
    <include file="student_homework.xml" relativeToChangelogFile="true"/>
    <include file="student_tutors.xml" relativeToChangelogFile="true"/>
    <include file="test-data.xml" relativeToChangelogFile="true"/>
</databaseChangeLog>
```

### src\main\resources\db\changelog\v1\homework.sql
```sql
CREATE TABLE homework
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    class      VARCHAR(255) NOT NULL, -- колонка для className
    subject    VARCHAR(255) NOT NULL,
    topic      VARCHAR(255) NOT NULL,
    difficulty INT          NOT NULL,
    tutor_id   INT          NOT NULL,
    CONSTRAINT fk_homework_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id)
);
```

### src\main\resources\db\changelog\v1\homework.xml
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

### src\main\resources\db\changelog\v1\homework_task.sql
```sql
CREATE TABLE homework_task
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    homework_id INT,
    task_id     INT,
    CONSTRAINT fk_homework_task_homework FOREIGN KEY (homework_id) REFERENCES homework (id),
    CONSTRAINT fk_homework_task_task FOREIGN KEY (task_id) REFERENCES task (id)
);
```

### src\main\resources\db\changelog\v1\homework_task.xml
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

### src\main\resources\db\changelog\v1\student.sql
```sql
CREATE TABLE student
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    password  VARCHAR(255) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname  VARCHAR(255) NOT NULL,
    birthdate DATE         NOT NULL,
    class     VARCHAR(255) NOT NULL, -- для className
    subject   VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE,
    phone     VARCHAR(255) NOT NULL UNIQUE,
    telegram  VARCHAR(255) UNIQUE
);
```

### src\main\resources\db\changelog\v1\student.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-student-table" author="yourName">
        <sqlFile path="student.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### src\main\resources\db\changelog\v1\student_homework.sql
```sql
CREATE TABLE students_homework
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    student_id  INT,
    homework_id INT,
    is_done      BOOLEAN,
    is_checked   BOOLEAN,
    score       INT,
    CONSTRAINT fk_students_homework_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_students_homework_homework FOREIGN KEY (homework_id) REFERENCES homework (id)
);
```

### src\main\resources\db\changelog\v1\student_homework.xml
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

### src\main\resources\db\changelog\v1\student_tutors.sql
```sql
CREATE TABLE students_tutors
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    tutor_id   INT,
    CONSTRAINT fk_students_tutors_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_students_tutors_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id)
);
```

### src\main\resources\db\changelog\v1\student_tutors.xml
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

### src\main\resources\db\changelog\v1\task.sql
```sql
CREATE TABLE task
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    photo_url  VARCHAR(255) NOT NULL,
    class      VARCHAR(255) NOT NULL, -- для className
    subject    VARCHAR(255) NOT NULL,
    topic      VARCHAR(255) NOT NULL,
    difficulty INT          NOT NULL,
    tutor_id   INT,
    CONSTRAINT fk_task_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id)
);
```

### src\main\resources\db\changelog\v1\task.xml
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

### src\main\resources\db\changelog\v1\test-data.sql
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

### src\main\resources\db\changelog\v1\test-data.xml
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

### src\main\resources\db\changelog\v1\tutor.sql
```sql
CREATE TABLE tutor
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    password  VARCHAR(255) NOT NULL,
    firstname VARCHAR(255) NOT NULL,
    lastname  VARCHAR(255) NOT NULL,
    birthdate DATE         NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE,
    phone     VARCHAR(255) NOT NULL UNIQUE,
    telegram  VARCHAR(255) UNIQUE
);
```

### src\main\resources\db\changelog\v1\tutor.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="create-tutor-table" author="yourName">
        <sqlFile path="tutor.sql" relativeToChangelogFile="true"/>
    </changeSet>
</databaseChangeLog>
```

### src\test\java\com\example\easy_learning\EasyLearningApplicationTests.java
```java
package com.example.easy_learning;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EasyLearningApplicationTests {

	@Test
	void contextLoads() {
	}

}
```


curl -X POST \
-F "task={\"className\":\"10A\",\"subject\":\"Math\",\"topic\":\"Equations\",\"difficulty\":3};type=application/json" \
-F "file=@C:/test.jpg" \
http://localhost:8080/api/tasks/with-file
