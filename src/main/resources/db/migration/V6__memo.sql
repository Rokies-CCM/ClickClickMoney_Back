CREATE TABLE IF NOT EXISTS memo (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumption_id  BIGINT NOT NULL,
    memo           VARCHAR(1000) NOT NULL,

    CONSTRAINT fk_consumption
        FOREIGN KEY (consumption_id)
        REFERENCES consumption(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_consumption(consumption_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;