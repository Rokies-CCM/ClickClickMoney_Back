CREATE TABLE IF NOT EXISTS consumption (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT  NOT NULL,
  category_id  BIGINT     NOT NULL,
  date         DATE    NOT NULL,
  amount       BIGINT  NOT NULL,

  CONSTRAINT fk_consumption_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT fk_consumption_category
    FOREIGN KEY (category_id)
    REFERENCES categories(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  INDEX idx_consumption_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
