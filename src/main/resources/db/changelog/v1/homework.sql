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