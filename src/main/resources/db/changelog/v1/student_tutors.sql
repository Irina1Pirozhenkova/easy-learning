CREATE TABLE students_tutors
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    tutor_id   INT,
    CONSTRAINT fk_st_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_st_tutor FOREIGN KEY (tutor_id) REFERENCES users (id) ON DELETE CASCADE
);