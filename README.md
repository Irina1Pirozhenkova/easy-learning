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
![image](https://github.com/user-attachments/assets/f176d304-ec39-4e9f-bf58-010d45345806)
#### Описание базы данных
##### Таблица student — хранит данные об ученике:
- id: уникальный идентификатор ученика.
- firstname, lastname: имя и фамилия.
- birthdate: дата рождения.
- class: класс.
- subject: предмет.
- email, phone, telegram: контакты.
- password: хеш пароля.

##### Таблица tutor — информация о репетиторе:
Аналогична student, но описывает репетитора.

##### Таблица homework — домашние задания:
- id: ID задания.
- class, subject, topic, difficulty: для какого класса и предмета, тема, уровень сложности.
- tutor_id: кто создал это ДЗ (внешний ключ на tutor).

##### Таблица task — отдельные задания внутри ДЗ:
- id, photo: задание может быть изображением.
- class, subject, topic, difficulty.
- tutor_id: автор задания.

##### Таблица homework_task — связь между домашками и заданиями:
Каждое ДЗ (homework_id) может содержать несколько заданий (task_id).

##### Таблица students_homework — кто какое ДЗ получил:
Связывает student_id и homework_id.

##### Таблица students_tutors — связь учеников и репетиторов:
Один репетитор может заниматься с несколькими учениками и наоборот.

##### Связи:
- Многие-ко-многим:
student ↔ tutor через students_tutors.
student ↔ homework через students_homework.
homework ↔ task через homework_task.
- Один-ко-многим:
tutor → homework, tutor → task.
  
## Лабораторная работа 1
- Развернута MySQL в Docker
- Разработаны ORM-модели с использованием Hibernate, настроены миграции
- Настроено хеширование паролей
- Написаны скрипты для заполнения базы данных тестовыми данными
- Реализован функционал для работы с данными в соответствии с тематикой приложения (сервисы, контроллеры, репозитории)

##### Разработка ORM-моделей
- Для взаимодействия с базой данных были разработаны ORM-модели с помощью Hibernate.
- Определены основные сущности: Homework, Task, Student, Tutor, а также сущности-связки HomeworkTask, StudentsHomework, StudentsTutors.
- Между сущностями настроены связи @OneToMany, @ManyToOne, @ManyToMany, обеспечивающие корректное отображение отношений.

##### Было реализовано:
- Для безопасности реализовано хеширование паролей с помощью bcrypt – популярного алгоритма, обеспечивающего защиту от атак на пароли.
- Проект создан с помощью Spring Initializr, на базе Spring Boot.
- Для управления структурой базы данных используется Liquibase – инструмент для миграций.

##### Были разработаны:
- Контроллеры: HomeworkController, StudentController, TaskController, TutorController
- Сервисы: HomeworkService, StudentService, TaskService, TutorService
- JPA-репозитории: HomeworkRepository, StudentRepository, TaskRepository, TutorRepository

## Лабораторная работа 2
- Разработаны CRUD-методы для работы с моделями
- Настроены маршруты и обработка запросов
- Для тестирования API использовался Postman (для проверки запросов)

#### Структура API

#### Homework:
**POST /api/homeworks** Создание домашнего задания.  
Request body: JSON с информацией о задании.  
Response: 200 OK (Homework), 400 Bad Request  

**GET /api/homeworks/{id}** Получить домашку по ID (опционально с задачами и студентами).  
Query param: ?full=true  
Response: 200 OK, 404 Not Found  

**GET /api/homeworks** Получить список всех домашних заданий.  
Response: 200 OK  

**PUT /api/homeworks/{id}** Обновить домашнее задание.  
Request body: JSON  
Response: 200 OK, 400 Bad Request, 404 Not Found  

**DELETE /api/homeworks/{id}** Удалить домашнее задание.  
Response: 204 No Content, 404 Not Found  

**POST /api/homeworks/{id}/tasks** Добавить задачи к домашнему заданию.  
Request body: список ID задач  
Response: 200 OK, 400 Bad Request  

**DELETE /api/homeworks/{id}/tasks** Удалить задачи из домашки.  
Request body: список ID задач  
Response: 200 OK, 404 Not Found

#### Task:
**POST /api/tasks** Создание новой задачи без файла.
Response: 200 OK (Task), 400 Bad Request (если тело запроса некорректное)
Request body:JSON с информацией о задании без файла

**POST /api/tasks/with-file** Создание новой задачи с файлом.
Response: 200 OK (Task), 400 Bad Request (если файл или данные некорректные)
Request body: JSON с информацией о задании с файлом

**GET /api/tasks/{id}** Получить задачу по ID.
Response: 200 OK, 404 Not Found (если не найдено)

**GET /api/tasks** Получить все задачи.
Response: 200 OK

**PUT /api/tasks/{id}** Обновить задачу.
Response: 200 OK, 404 Not Found
Request body: JSON 

**DELETE /api/tasks/{id}** Удалить задачу по ID.
Response: 204 No Content, 404 Not Found

**GET /api/tasks/{id}/photo** Получить фото задачи по ID.
Response: 200 OK (image/jpeg или image/png), 404 Not Found



##### Примеры запросов и ответов описаны в отчёте, который доступен по ссылке
https://docs.google.com/document/d/1odeRMj0IMw941y2SPaJtfBGpDKGm13SqU9hXOAfoywI/edit?usp=sharing

