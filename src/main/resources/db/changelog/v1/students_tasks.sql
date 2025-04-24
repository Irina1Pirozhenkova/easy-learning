CREATE TABLE students_tasks
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    task_id    INT,
    is_done    BOOLEAN,
    is_checked BOOLEAN,
    score      INT,
    CONSTRAINT fk_sh_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_sh_homework FOREIGN KEY (task_id) REFERENCES task (id) ON DELETE CASCADE
);