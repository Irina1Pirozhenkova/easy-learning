CREATE TABLE homework_task (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    homework_id INT,
    task_id     INT,
    CONSTRAINT fk_ht_homework FOREIGN KEY (homework_id) REFERENCES homework(id) ON DELETE CASCADE,
    CONSTRAINT fk_ht_task     FOREIGN KEY (task_id)     REFERENCES task(id)      ON DELETE CASCADE
);