# Easy Learning – инновационная образовательная платформа, позволяющая автоматизировать процесс выдачи домашних заданий ученикам.
### Цель проекта: Создать удобную веб-платформу, которая позволит репетиторам эффективно управлять домашними заданиями, группировать их в комплексы и раздавать ученикам в упорядоченной форме. 

### Основные проблемы, которые решает платформа:
- Разрозненность заданий и отсутствие единого пространства для их хранения.
- Затраты времени на раздачу заданий.
- Необходимость гибкости в управлении учебными материалами.

### Функциональные требования
- Регистрация и авторизация пользователей (репетиторы, ученики).
- Панель управления для создания, редактирования и удаления заданий.
- Группировка заданий в домашние работы.
- Назначение заданий ученикам.

## Лабораторная работа 0 
- Разработана логическую схема базы данных
- Определена структура API
- Создан Git-репозиторий
#### Схема базы данных
![image](https://github.com/user-attachments/assets/63fd2a9a-376a-45ce-8fae-95afd818ae0f)

#### Описание базы данных
##### Таблица **users** — общая информация о пользователе (и студенте, и тьюторе)
- **id**  INTEGER – уникальный идентификатор пользователя  
- **email**  VARCHAR – электронная почта (логин), уникальное  
- **password**  VARCHAR – захешированный пароль  
- **personalInfo.firstname**  VARCHAR – имя  
- **personalInfo.lastname**  VARCHAR – фамилия  
- **personalInfo.birthdate**  DATE – дата рождения  
- **personalInfo.phone**  VARCHAR – номер телефона, уникальное  
- **personalInfo.telegram**  VARCHAR – ник в Telegram, уникальное  

##### Таблица **user_roles** — роли пользователя  
- **id**  INTEGER – PK  
- **user_id**  INTEGER → users.id  
- **role**  VARCHAR – STUDENT или TUTOR  

##### Таблица **user_subject_classes** — сочетания «предмет ↔ класс»  
> (реализовано как `@ElementCollection` в User.personalInfo)  
- **user_id**  INTEGER → users.id  
- **subject**  VARCHAR – ENUM из {MATH, PHYSICS, …, ENGLISH}  
- **class_level**  VARCHAR – ENUM из {CLASS_1…CLASS_11}  

##### Таблица **task** — задания, создаваемые тьюторами  
- **id**  INTEGER – PK  
- **photo_url**  VARCHAR – ссылка на картинку с условием задачи  
- **class**  VARCHAR – ENUM ClassLevel (класс, для которого задача)  
- **subject**  VARCHAR – ENUM Subject  
- **topic**  VARCHAR – краткий заголовок/тема задачи  
- **description**  VARCHAR – подробное описание  
- **difficulty**  INTEGER – сложность (1–10 и т. п.)  
- **tutor_id**  INTEGER → users.id  

##### Таблица **students_tasks** — статус выполнения задания учеником  
- **id**  INTEGER – PK  
- **student_id**  INTEGER → users.id  
- **task_id**  INTEGER → task.id  
- **is_done**  BOOLEAN – пометил ли ученик как «сделал»  
- **is_checked**  BOOLEAN – проверил ли тьютор  
- **score**  INTEGER – выставленный балл  

##### Таблица **students_tutors** — связь «ученик ↔ тьютор»  
- **id**  INTEGER – PK  
- **student_id**  INTEGER → users.id (роль STUDENT)  
- **tutor_id**  INTEGER → users.id (роль TUTOR)  

#### Кардинальности и типы связей
1. **`users` ↔ `user_roles`**  
   - Тип: **1-к-многим**  
   - Один пользователь (`users.id`) может иметь несколько записей в `user_roles` (несколько ролей), но каждая запись в `user_roles` принадлежит ровно одному пользователю.

2. **`users` ↔ `user_subject_classes`**  
   - Тип: **1-к-многим**
   - Один пользователь хранит множество сочетаний «предмет ↔ класс» (`user_subject_classes`), каждое из которых связано с одним `users.id`.

3. **`users` (Tutor) ↔ `task`**  
   - Тип: **1-к-многим**  
   - Один тьютор (`users.id` c ролью TUTOR) может создавать много задач (`task.tutor_id`), но у каждой задачи ровно один автор-тьютор.

4. **`task` ↔ `students_tasks`**  
   - Тип: **1-к-многим**  
   - Одна задача (`task.id`) может встречаться во многих записях `students_tasks` (разные ученики), но каждая запись `students_tasks` ссылается на одну задачу.

5. **`users` (Student) ↔ `students_tasks`**  
   - Тип: **1-к-многим**  
   - Один ученик (`users.id` c ролью STUDENT) может иметь много записей в `students_tasks` (для разных задач), но каждая запись относится к одному ученику.

6. **`students_tasks`**  
   - Фактически реализует **отношение многие-ко-многим** между **учениками** и **задачами**, обогащённое дополнительными атрибутами (`is_done`, `score` и т. д.).  

7. **`users` (Student) ↔ `students_tutors`**  
   - Тип: **1-к-многим**  
   - Один ученик (`users.id` с ролью STUDENT) может быть связан сразу с несколькими репетиторами через `students_tutors.student_id`.

8. **`users` (Tutor) ↔ `students_tutors`**  
   - Тип: **1-к-многим**  
   - Один репетитор (`users.id` с ролью TUTOR) может вести много учеников через `students_tutors.tutor_id`.

9. **`students_tutors`**  
   - Фактически реализует **отношение многие-ко-многим** между **учениками** и **тьюторами**.


  
## Лабораторная работа 1
- Развернута MySQL в Docker
- Разработаны ORM-модели с использованием Hibernate, настроены миграции
- Настроено хеширование паролей
- Написаны скрипты для заполнения базы данных тестовыми данными
- Реализован функционал для работы с данными в соответствии с тематикой приложения (сервисы, контроллеры, репозитории)

##### Разработка ORM-моделей
- Для взаимодействия с базой данных были разработаны ORM-модели с помощью Hibernate.
- Определены основные сущности:
1. **User** (`users`)  
   - PK: `id`  
   - Поля: `email`, `password`, `personalInfo`,  
     коллекция `roles` (через `user_roles`), коллекция `subjectClassPairs` (через `user_subject_classes`),  
     связи на созданные задачи (`tasks`), домашки (`studentsTasks`) и связи ученик–тьютор (`tutors`, `students`).

2. **Role** (`user_roles`)   
   - PK: `id`  
   - FK: `user_id` → `users.id`  
   - Поле: `role` (STUDENT / TUTOR)

3. **SubjectClassPair** (`user_subject_classes`)  
   - PK (составной): `user_id` + `subject` + `class_level`

4. **Task** (`task`)  
   - PK: `id`  
   - FK: `tutor_id` → `users.id`  
   - Поля: `photo_url`, `class_level`, `subject`, `topic`, `description`, `difficulty`

5. **StudentsTasks** (`students_tasks`)  
   - PK: `id`  
   - FK: `student_id` → `users.id`  
   - FK: `task_id` → `task.id`  
   - Поля: `is_done`, `is_checked`, `score`

6. **StudentsTutors** (`students_tutors`)  
   - PK: `id`  
   - FK: `student_id` → `users.id`  
   - FK: `tutor_id` → `users.id`  

- Между сущностями настроены связи @OneToMany, @ManyToOne, @ManyToMany, обеспечивающие корректное отображение отношений.

##### Было реализовано:
- Для безопасности реализовано хеширование паролей с помощью bcrypt – популярного алгоритма, обеспечивающего защиту от атак на пароли.
- Проект создан с помощью Spring Initializr, на базе Spring Boot.
- Для управления структурой базы данных используется Liquibase – инструмент для миграций.

##### Были разработаны:
- **Контроллеры**:
  - `AuthController`  
  - `ViewController`  

- **Сервисы** (интерфейсы + реализации в `service/impl`):
  - `AuthService` → `AuthServiceImpl`  
  - `TaskService` → `TaskServiceImpl`  
  - `StudentsTutorsService` → `StudentsTutorsServiceImpl`  
  - `UserService` → `UserServiceImpl`

- **JPA-репозитории**:
  - `UserRepository`  
  - `TaskRepository`  
  - `StudentsTasksRepository`  
  - `StudentsTutorsRepository`  

## Лабораторная работа 2
- Разработаны CRUD-методы для работы с моделями
- Настроены маршруты и обработка запросов
- Для тестирования API использовался Postman (для проверки запросов)

#### Структура API



## Лабораторная работа 3
#### Сделано:
- Регистрация нового пользователя
- Вход в систему и получение JWT-токена
- Настройка middleware для защиты API
- Проверка валидности токена
- Ограничение доступа к определённым эндпоинтам

#### Проверка работы аутентификации через Postman

##### 1. Регистрация тьютора
- URL: POST http://localhost:8080/api/v1/auth/register/tutor
- Body (JSON):{ "email": "tutor1@mail.ru", "password": "12345"}
- Response (201 Created): { "email": "tutor1@mail.ru",  "password": "$2a$10$...","userType": "tutor"}
##### 2. Логин тьютора
- URL: POST http://localhost:8080/api/v1/auth/login
- Body (JSON):{"username": "tutor1@mail.ru",  "password": "12345"}
- Response (200 OK):{ "id": 3, "username": "tutor1@mail.ru", "accessToken": "...", "refreshToken": "..."}
##### 3. Получение одного тьютора
- URL: GET http://localhost:8080/api/tutor/3
- Authorization: Bearer {accessToken}
- Response (200 OK):{"id": 3,"email": "tutor1@mail.ru","tasks": [...], "homeworks": [...]}
##### 4. Получение всех тьюторов
- URL: GET http://localhost:8080/api/tutor
- Authorization: Bearer {accessToken}
- Response (200 OK) — если авторизован тьютор
- Response (403 Forbidden) — если студент
##### 5. Регистрация тьютором студента
- URL: POST http://localhost:8080/api/students
- Authorization: Bearer {accessToken} (только тьютор)
- Body (JSON):{  "email": "student1@mail.ru",  "password": "12345"}
- Response (201 Created):{  "id": 4,  "email": "student1@mail.ru", "password": null}
##### 6. Логин студента
- URL: POST http://localhost:8080/api/v1/auth/login
- Body (JSON):{  "username": "student1@mail.ru",  "password": "12345"}
- Response (200 OK):{  "id": 4,  "username": "student1@mail.ru",  "accessToken": "...",  "refreshToken": "..."}
##### 7. Получение информации о себе (студент)
- URL: GET http://localhost:8080/api/students/4?full=true
- Authorization: Bearer {accessToken}
- Response (200 OK):{ "id": 4,"email": "student1@mail.ru",  "homeworks": [...], "tutors": [...]}
- Response (403 Forbidden) — если пытается получить чужие данные
##### 8. Получение всех студентов
- URL: GET http://localhost:8080/api/students
- Authorization: Bearer {accessToken} (только тьютор)
- Response (200 OK) — если тьютор
- Response (403 Forbidden) — если студент
##### 9. Обновление студента (изменение пароля)
- URL: PUT http://localhost:8080/api/students/4
- Authorization: Bearer {accessToken}
- Body (JSON):{ "password": "новыйПароль123",  "studentPersonalInfo": { ... }}
- Response (200 OK) — если обновляет сам себя
- Response (403 Forbidden) — если другой пользователь

#### Описание модели пользователя и JWT-аутентификации

##### JWT-система:
При логине или регистрации выдаются два токена: accessToken и refreshToken.
accessToken — короткоживущий (30 минут), используется в заголовке Authorization.
refreshToken — долгоживущий (300 минут), используется для обновления accessToken.

##### Принцип работы:
Пользователь логинится → получает токены.
accessToken передаётся в каждый запрос, где требуется авторизация.
Middleware (JwtTokenFilter) извлекает токен и подставляет в SecurityContext.
Контроллеры через SecurityContextHolder знают, кто сделал запрос.

##### Безопасность:
Доступ к критичным методам (создание/обновление студентов, получение всех студентов) ограничен по роли (только для тьюторов).
Студенты могут видеть только свои данные.
Валидация токенов, кастомные 401 и 403 статусы.

##### Модель пользователя:
Student и Tutor имеют email, password (хранится в виде bcrypt-хэша), личную информацию и связи (задания, репетиторы, домашки).
Для авторизации используются кастомные реализации UserDetails (StudentJwtEntity, TutorJwtEntity).

## Лабораторная работа 4
Будет позже
