# REST-Assured practice project

## Где что лежит

- `base/BaseApiTest` — базовая настройка REST-Assured
- `base/BaseAuthorizedApiTest` — общий `BeforeEach`, который поднимает авторизованную сессию
- `auth/AuthApiClient` — шаги авторизации: register -> login -> refresh
- `auth/AuthSession` — объект с access token, refresh token, email, password
- `auth/TestUserFactory` — генерация уникального тестового пользователя
- `models/*` — request/response модели
- `tests/AuthorizedApiExamplesTest` — 2 готовых примера тестов
- `tests/BlogApiHomeworkTest` — заготовки для самостоятельной практики

## Как токен попадает в тесты

В `BaseAuthorizedApiTest` перед каждым тестом автоматически выполняется:

- регистрация нового пользователя
- логин
- refresh токена
- сборка `authorizedRequestSpec`

После этого в тесте доступны:

- `authSession` — данные текущей авторизованной сессии
- `authorizedRequestSpec` — готовый `RequestSpecification` с заголовком `Authorization: Bearer <token>`

Пример использования:

```java
//given()
//        .spec(authorizedRequestSpec)
//.when()
//        .get("/api/profile")
//.then()
//        .statusCode(200);
```

## Что уже реализовано

1. `GET /api/profile -> shouldReturnCurrentUserProfile`
2. `POST /api/posts -> shouldCreatePostForAuthorizedUser`

## Что нужно сделать

Открыть класс `BlogApiHomeworkTest` и постепенно реализовать остальные тесты.

## Как запустить

По умолчанию используется:

```properties
base.url=http://localhost:3000
```

Можно поменять в `src/test/resources/application.properties`
или передать через system property:

```bash
mvn test -Dbase.url=http://localhost:3000
```

## Важно

Хорошая практика — не писать register/login/refresh прямо внутри каждого теста.
Лучше вынести это:

- либо в base class
- либо в отдельный auth client / helper
- а в тесты передавать уже готовый request spec с токеном

Так тесты остаются короткими и читаемыми, а авторизация меняется в одном месте.
