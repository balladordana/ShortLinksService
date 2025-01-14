# Конфигурируемый сокращатель ссылок

## Описание
Приложение для сокращения длинных URL с возможностью управления ссылками и их параметрами. Поддерживаются функции ограничения количества переходов, настройки времени жизни ссылки, а также идентификация пользователей.

## Основные возможности
1. **Сокращение ссылки:**
   - Пользователи могут преобразовывать длинные URL в короткие, причём разные пользователи получают уникальные ссылки на один и тот же URL.
2. **Лимит переходов:**
   - Подсчёт количества переходов по каждой ссылке с блокировкой после достижения лимита.
3. **Время жизни ссылки:**
   - Возможность настройки времени жизни ссылки с автоматическим удалением истёкших ссылок. Реализована конфигурация через внешний файл.
4. **Идентификация пользователя:**
   - Генерация уникального UUID при первом запросе. Все операции с ссылками доступны только создателю.
5. **Переход по короткой ссылке:**
   - Автоматическое перенаправление на исходный ресурс при вводе короткой ссылки.
6. **Обновление лимита переходов:**
   - Возможность изменения лимита переходов по ссылке.
7. **Удаление ссылки:**
   - Удаление ссылки из системы.

## API Эндпоинты

### Сокращение ссылки
`POST /api/url/short`
- **Параметры:**
  - `UUID` (опционально): Идентификатор пользователя.
  - `longUrl`: Длинный URL для сокращения.
  - `clickLimit` (по умолчанию: 10): Лимит переходов по ссылке.
  - `daysToLive` (по умолчанию: 1): Время жизни ссылки в днях.
- **Пример ответа:**
  ```json
  {
      "UUID": "915edcc8-ee43-43a3-83aa-d45a88d663ed",
      "shortURL": "clck.ru/MTg5Mz",
      "clickLimit": 10,
      "expired": false
  }
  ```

### Получение всех ссылок пользователя
`GET /api/url/short/{userUUID}`
- **Параметры:**
  - `userUUID`: Идентификатор пользователя.
- **Пример ответа:**
  ```json
  [
      {
          "shortUrl": "clck.ru/MTg5Mz",
          "longUrl": "https://example.com",
          "clickLimit": 10,
          "daysToLive": 1
      }
  ]
  ```

### Перенаправление по короткой ссылке
`GET /api/url/redirect`
- **Параметры:**
  - `userUUID`: Идентификатор пользователя.
  - `shortUrl`: Короткий URL для перехода.
- **Пример ответа:** HTTP 302 Redirect на исходный URL.

### Обновление лимита переходов
`POST /api/url/update`
- **Параметры:**
  - `userUUID`: Идентификатор пользователя.
  - `shortUrl`: Короткий URL.
  - `clickLimit`: Новый лимит переходов.
- **Пример ответа:**
  ```json
  {
      "message": "URL clickLimit is updated"
  }
  ```

### Удаление ссылки
`POST /api/url/delete`
- **Параметры:**
  - `userUUID`: Идентификатор пользователя.
  - `shortUrl`: Короткий URL для удаления.
- **Пример ответа:**
  ```json
  {
      "message": "URL is deleted"
  }
  ```

## Конфигурация
Приложение использует внешний файл конфигурации для задания следующих параметров:
- Максимальное время жизни ссылки.

Пример файла `application.properties`:
```properties
url.shortener.default-days-to-live=1
```

## Сборка и запуск
1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/balladordana/url-shortener.git
   ```
2. Перейдите в директорию проекта и запустите приложение:
   ```bash
   mvn spring-boot:run
   ```

## Требования
- Java 17+
- Spring Boot 3.0+
- Maven 3.8+


