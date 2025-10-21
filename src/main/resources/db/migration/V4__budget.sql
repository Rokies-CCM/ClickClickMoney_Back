CREATE TABLE IF NOT EXISTS `budget` (
  `id`           INT PRIMARY KEY AUTO_INCREMENT,
  `user_id`      BIGINT      NOT NULL,
  `budget_month`   DATE  NOT NULL,
  `category_id`  BIGINT         NOT NULL,
  `amount`       BIGINT      NOT NULL,

  CONSTRAINT `fk_budget_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  CONSTRAINT `fk_budget_category`
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  KEY `idx_budget_user_month` (`user_id`, `budget_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
