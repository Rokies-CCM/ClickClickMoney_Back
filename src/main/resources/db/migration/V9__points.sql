-- 지갑(유저별 1행)
CREATE TABLE IF NOT EXISTS points_wallet (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id BIGINT NOT NULL UNIQUE,
                                             balance BIGINT NOT NULL DEFAULT 0,
                                             total_earned BIGINT NOT NULL DEFAULT 0,
                                             total_spent BIGINT NOT NULL DEFAULT 0,
                                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             CONSTRAINT fk_points_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 원장(적립/차감 이력)
CREATE TABLE IF NOT EXISTS points_tx (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         user_id BIGINT NOT NULL,
                                         delta BIGINT NOT NULL,              -- 적립은 양수, 차감은 음수
                                         reason VARCHAR(64) NOT NULL,        -- 예: MISSION_REWARD, REDEEM
    source VARCHAR(64) NULL,            -- 예: quiz, web
    meta_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_points_tx_user_created (user_id, created_at),
    CONSTRAINT fk_points_tx_user FOREIGN KEY (user_id) REFERENCES users(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
