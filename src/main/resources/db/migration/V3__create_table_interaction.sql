CREATE TABLE interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    query TEXT,
    response TEXT,
    usuario VARCHAR(255),
    timestamp DATETIME NOT NULL
);
