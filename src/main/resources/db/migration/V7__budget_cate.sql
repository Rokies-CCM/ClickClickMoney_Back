ALTER TABLE `budget`
  DROP FOREIGN KEY IF EXISTS `fk_budget_category`;

ALTER TABLE `budget`
  DROP COLUMN IF EXISTS `category_id`;

UPDATE categories
SET type = '선택 지출'
WHERE name = '카페/간식' AND type = '선택지출';