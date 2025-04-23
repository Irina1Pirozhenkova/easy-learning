CREATE TABLE task
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    photo_url   VARCHAR(255) NOT NULL,
    class       VARCHAR(50)  NOT NULL,
    subject     VARCHAR(50)  NOT NULL,
    topic       VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    difficulty  INT          NOT NULL,
    tutor_id    INT,
    CONSTRAINT fk_task_tutor FOREIGN KEY (tutor_id) REFERENCES users (id)
);