CREATE TABLE tutor
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    password  VARCHAR(255) NOT NULL,
    firstname VARCHAR(255),
    lastname  VARCHAR(255),
    birthdate DATE,
    email     VARCHAR(255) NOT NULL UNIQUE,
    phone     VARCHAR(255) UNIQUE,
    telegram  VARCHAR(255) UNIQUE
);
