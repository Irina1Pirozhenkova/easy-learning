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