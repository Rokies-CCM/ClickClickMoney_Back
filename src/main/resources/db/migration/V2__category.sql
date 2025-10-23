CREATE TABLE IF NOT EXISTS categories (
  id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50)  NOT NULL,
  type VARCHAR(20)  NOT NULL,
  CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



INSERT INTO categories (name, type) VALUES
  ('생활','필수 지출'),
  ('식비','필수 지출'),
  ('교통','선택 지출'),
  ('주거','필수 지출'),
  ('통신','선택 지출'),
  ('쇼핑','선택 지출'),
  ('카페/간식','선택지출'),
  ('의료/건강','필수 지출'),
  ('문화/여가','선택 지출'),
  ('기타','기타')
ON DUPLICATE KEY UPDATE type = VALUES(type);

