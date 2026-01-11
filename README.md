# Filmorate

REST API для оценки фильмов и общения между пользователями.

## 🚀 Запуск проекта

1. **Клонируйте репозиторий:**
```bash
git clone <repository-url>
cd filmorate
```

2. **Соберите проект:**
```bash
mvn clean package
```

3. **Запустите приложение:**
```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

## 📁 Структура проекта

```
src/main/java/ru/yandex/practicum/filmorate/
├── controller/     # REST контроллеры
├── model/         # Модели данных
├── service/       # Бизнес-логика
├── storage/       # Работа с базой данных
├── dto/           # Data Transfer Objects
└── exception/     # Кастомные исключения
```

## 🗄️ Схема базы данных

![Схема базы данных Filmorate](docs/filmorate-db.png)

Схема отражает структуру базы данных приложения Filmorate и включает следующие сущности:

### Таблицы:
- **users** — пользователи
- **films** — фильмы
- **mpa** — рейтинги MPA (G, PG, PG-13, R, NC-17)
- **genre** — жанры фильмов
- **film_genres** — связь «многие ко многим» между фильмами и жанрами
- **likes** — лайки пользователей под фильмами
- **friendship** — односторонняя дружба между пользователями

### Связи:
- `films.mpa_id` → `mpa.id`
- `film_genres.film_id` → `films.id`, `film_genres.genre_id` → `genre.id`
- `likes.film_id` → `films.id`, `likes.user_id` → `users.id`
- `friendship.user_id` → `users.id`, `friendship.friend_id` → `users.id`

Все связующие таблицы (`film_genres`, `likes`, `friendship`) используют составные первичные ключи. Дружба реализована как односторонняя: для взаимной дружбы требуется две записи в таблице `friendship`.

## 📋 API Endpoints

### Фильмы
- `GET /films` — получить все фильмы
- `GET /films/{id}` — получить фильм по ID
- `POST /films` — создать фильм
- `PUT /films` — обновить фильм
- `PUT /films/{id}/like/{userId}` — поставить лайк
- `DELETE /films/{id}/like/{userId}` — удалить лайк
- `GET /films/popular?count={n}` — получить топ-N популярных фильмов

### Пользователи
- `GET /users` — получить всех пользователей
- `GET /users/{id}` — получить пользователя по ID
- `POST /users` — создать пользователя
- `PUT /users` — обновить пользователя
- `PUT /users/{id}/friends/{friendId}` — добавить друга
- `DELETE /users/{id}/friends/{friendId}` — удалить друга
- `GET /users/{id}/friends` — получить друзей пользователя
- `GET /users/{id}/friends/common/{otherId}` — получить общих друзей

### MPA и жанры
- `GET /mpa` — получить все рейтинги MPA
- `GET /mpa/{id}` — получить рейтинг MPA по ID
- `GET /genres` — получить все жанры
- `GET /genres/{id}` — получить жанр по ID

## 🛠️ Технологии

- **Java 11+**
- **Spring Boot 2.x**
- **H2 Database** (встроенная)
- **Maven** (сборка)
- **Lombok** (уменьшение boilerplate кода)
- **JDBC Template** (работа с БД)

## 📊 Примеры SQL-запросов (H2 совместимые)

### Топ-10 самых популярных фильмов (по количеству лайков):
```sql
SELECT f.id, f.name, COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC
LIMIT 10;
```

### Получить всех друзей пользователя (например, user_id = 123):
```sql
SELECT u.*
FROM users u
JOIN friendship f ON u.id = f.friend_id
WHERE f.user_id = 123;
```

### Найти общих друзей двух пользователей (123 и 456):
```sql
SELECT u.*
FROM users u
JOIN friendship f1 ON u.id = f1.friend_id
JOIN friendship f2 ON u.id = f2.friend_id
WHERE f1.user_id = 123 AND f2.user_id = 456;
```

### Получить фильм с его рейтингом MPA и жанрами (H2 совместимый):
```sql
SELECT f.*, m.name AS mpa_name, 
       STRING_AGG(g.name, ', ') AS genres
FROM films f
LEFT JOIN mpa m ON f.mpa_id = m.id
LEFT JOIN film_genres fg ON f.id = fg.film_id
LEFT JOIN genre g ON fg.genre_id = g.id
WHERE f.id = 1
GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name;
```

## 🔧 Конфигурация

Файл `application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./db/filmorate
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

spring.sql.init.mode=always
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 📝 Примеры запросов

### Создание пользователя:
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "login": "userlogin",
    "name": "User Name",
    "birthday": "1990-01-01"
  }'
```

### Создание фильма:
```bash
curl -X POST http://localhost:8080/films \
  -H "Content-Type: application/json" \
  -d '{
    "name": "The Matrix",
    "description": "Sci-fi action film",
    "releaseDate": "1999-03-31",
    "duration": 136,
    "mpa": {"id": 4},
    "genres": [{"id": 6}, {"id": 4}]
  }'
```

## 📄 Лицензия

Проект создан в рамках учебного курса Яндекс.Практикум.