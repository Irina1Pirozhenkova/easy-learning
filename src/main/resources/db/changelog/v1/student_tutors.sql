CREATE TABLE students_tutors
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    tutor_id   INT,
    CONSTRAINT fk_students_tutors_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_students_tutors_tutor FOREIGN KEY (tutor_id) REFERENCES tutor (id)
);
