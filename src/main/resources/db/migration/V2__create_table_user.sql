CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER'
);

-- Seed user 'pedro' with password 'senha123' hashed with BCrypt
INSERT INTO users (username, password, role) VALUES ('pedro', '$2a$10$FVkyMKFVlXyAfzcE3y9/D.6u3KA1T8OeoFiaCjlYPIXIBbCnbIsJq', 'USER');
