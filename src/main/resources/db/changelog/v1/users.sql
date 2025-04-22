CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,

    firstname     VARCHAR(255),
    lastname      VARCHAR(255),
    birthdate     DATE,
    phone         VARCHAR(255) UNIQUE,
    telegram      VARCHAR(255) UNIQUE
);

CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_subject_classes (
    user_id      INT NOT NULL,
    subject      VARCHAR(50) NOT NULL,
    class_level  VARCHAR(50) NOT NULL,
    PRIMARY KEY(user_id, subject, class_level),
    CONSTRAINT fk_user_sc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_sc_user ON user_subject_classes(user_id);