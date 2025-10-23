INSERT INTO categories (name, type) VALUES
    ('구독','선택 지출')
ON DUPLICATE KEY UPDATE type = VALUES(type);