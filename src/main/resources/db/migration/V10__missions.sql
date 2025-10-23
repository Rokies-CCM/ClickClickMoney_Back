-- mission_state: 사용자 x 날짜 x 미션코드 상태
CREATE TABLE IF NOT EXISTS mission_state (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             date DATE NOT NULL,
                                             mission_code VARCHAR(32) NOT NULL,
    completed TINYINT(1) NOT NULL DEFAULT 0,
    reward_claimed TINYINT(1) NOT NULL DEFAULT 0,
    completed_at DATETIME NULL,
    claimed_at DATETIME NULL,
    CONSTRAINT uk_state UNIQUE (user_id, date, mission_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- quiz_attempts: 퀴즈 제출 이력
CREATE TABLE IF NOT EXISTS quiz_attempts (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             date DATE NOT NULL,
                                             score INT NOT NULL,
                                             passed TINYINT(1) NOT NULL,
    meta_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX ix_quiz_user_date_passed (user_id, date, passed)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
