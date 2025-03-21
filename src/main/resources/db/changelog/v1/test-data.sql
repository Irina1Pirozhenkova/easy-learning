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
