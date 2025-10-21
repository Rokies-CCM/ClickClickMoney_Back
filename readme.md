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
POST /api/consumptions/save
```
{
    "items": [
    { "category": "식비",   "amount": 24500, "date": "2025-10-19" },
    { "category": "교통",   "amount": 15000, "date": "2025-10-20" },
    { "category": "쇼핑",   "amount": 99000, "date": "2025-10-20" }
    ]
}
```

GET /api/consumption/load
```
{
    "success": true,
    "message": null,
    "data": {
        "content": [
            {
                "id": 3,
                "date": "2025-10-20",
                "amount": 99000,
                "categoryName": "쇼핑",
                "categoryType": "선택 지출"
            },
            {
                "id": 2,
                "date": "2025-10-20",
                "amount": 15000,
                "categoryName": "교통",
                "categoryType": "선택 지출"
            },
            {
                "id": 1,
                "date": "2025-10-19",
                "amount": 24500,
                "categoryName": "식비",
                "categoryType": "필수 지출"
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 20,
            "sort": {
                "empty": true,
                "sorted": false,
                "unsorted": true
            },
            "offset": 0,
            "paged": true,
            "unpaged": false
        },
        "totalPages": 1,
        "totalElements": 3,
        "last": true,
        "size": 20,
        "number": 0,
        "sort": {
            "empty": true,
            "sorted": false,
            "unsorted": true
        },
        "numberOfElements": 3,
        "first": true,
        "empty": false
    }
}
```

PUT /api/consumptions/{consumption_id}
```
파라미터 추가
/api/consumptions/1?date=2025-10-01&category=식비&amount=18000
/api/consumptions/1?date=2025-10-01
/api/consumptions/1?amount=22000
/api/consumptions/1?category=교통
```

DELETE /api/consumptions/{consumption_id}
```
출력:
{
    "success": true,
    "message": null,
    "data": null
}
```


POST /api/budgets
```
{
  "month": "2025-10",
  "category": "식비",
  "amount": 100000
}
```


GET /api/budgets?month={조회할 날짜}
```
파라미터 추가
/api/budgets?month=2025-10
    
    
출력:
    {
        "id": 1,
        "month": "2025-10",
        "category": "식비",
        "amount": 100000
    }
```