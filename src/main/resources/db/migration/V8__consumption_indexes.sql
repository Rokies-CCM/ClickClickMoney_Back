CREATE INDEX ix_cons_user_date ON consumption(user_id, date);
CREATE INDEX ix_cons_category ON consumption(category_id);
-- (선택) 해시열이 있다면 유니크키: (user_id, source_hash)