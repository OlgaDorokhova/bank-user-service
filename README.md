# Bank User Service 

## *Система управления пользователями и переводами.* 

### Технологии

- Java 11, Spring Boot 2.7, Spring Security + JWT
- PostgreSQL, Redis, Docker
- Swagger, Testcontainers

***Быстрый запуск***

```bash
1. Запустить БД и Redis
docker-compose up -d

2. Запустить приложение
./mvnw spring-boot:run
Swagger: http://localhost:8080/swagger-ui.html

Тестовые пользователи
Логин (email/phone)	Пароль
ivan@mail.ru	password123
maria@mail.ru	password123
79201234567	password123

API

POST	/api/auth/login     Логин с JWT
GET	/api/users/search   Поиск пользователей
GET	/api/users/me       Информация о себе
PUT	/api/users/me       Обновить email/phone
POST	/api/transfers      Перевод денег

Тесты
bash
./mvnw test
Остановка
bash
docker-compose down -v