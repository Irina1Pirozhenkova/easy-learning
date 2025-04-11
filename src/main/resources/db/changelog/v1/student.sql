CREATE TABLE student
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    password  VARCHAR(255),
    firstname VARCHAR(255),
    lastname  VARCHAR(255),
    birthdate DATE,
    class     VARCHAR(255), -- для className
    subject   VARCHAR(255),
    email     VARCHAR(255) NOT NULL UNIQUE,
    phone     VARCHAR(255) UNIQUE,
    telegram  VARCHAR(255) UNIQUE
);
