CREATE TABLE homework_task
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    homework_id INT,
    task_id     INT,
    CONSTRAINT fk_homework_task_homework FOREIGN KEY (homework_id) REFERENCES homework (id),
    CONSTRAINT fk_homework_task_task FOREIGN KEY (task_id) REFERENCES task (id)
);
