DB생성 및 권한부여

```
CREATE DATABASE clickdb CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'click'@'localhost' IDENTIFIED BY 'click';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX
ON clickdb.* TO 'click'@'localhost';

FLUSH PRIVILEGES;

SHOW GRANTS FOR 'click'@'localhost';
```




명세서 

POST /api/auth/register
```
{
    "username":"test",
    "password":"test1234"
}
```

POST /api/auth/login
```
{
    "username":"test",
    "password":"test1234"
}
```
POST /api/ai/llm
```
{
  "question": "이번 달 과소비 요약 부탁해",
  "items": [
    { "category": "식비",   "amount": 24500, "date": "2025-10-19" },
    { "category": "교통",   "amount": 15000, "date": "2025-10-20" },
    { "category": "쇼핑",   "amount": 99000, "date": "2025-10-20" }
  ]
}
```
POST /api/consumption
```
{
    "items": [
    { "category": "식비",   "amount": 24500, "date": "2025-10-19" },
    { "category": "교통",   "amount": 15000, "date": "2025-10-20" },
    { "category": "쇼핑",   "amount": 99000, "date": "2025-10-20" }
    ]
}
```