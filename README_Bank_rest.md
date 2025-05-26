# Bank Cards Service

Bank Cards Service — это REST-сервис, разработанный на базе Spring Boot, предназначенный для управления банковскими картами. Сервис предоставляет функциональность по получению баланса, просмотру и управлению картами, а также использует JWT-аутентификацию и шифрование данных.

## Требования

Перед запуском убедитесь, что на вашей машине установлены следующие компоненты:

* Java 17 или выше
* Maven 3.8+
* Docker и Docker Compose

## Инструкция по запуску

### 1. Создание файла `.env`

Создайте в корневом каталоге проекта файл `.env` со следующим содержимым:

```env
POSTGRES_DB=db_name
POSTGRES_USER=db_user
POSTGRES_PASSWORD=db_password
JWT_SECRET_KEY=MySuperSecretKeyWith32+Characters!
ENCRYPTION_KEY=N123221593dd45lKg_28Dh$4jG&8lM9!R5k
```

### 2. Запуск через Docker Compose

Для запуска всех необходимых сервисов выполните:

```bash
docker-compose up --build
```

Будут подняты следующие контейнеры:

* `bank` — основной микросервис приложения на Spring Boot (порт 8083)
* `postgres` — база данных PostgreSQL (порт 5555)

## Swagger

Документация API доступна по следующему адресу:

* **Swagger UI**: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
* **OpenAPI YAML**: [http://localhost:8083/docs/openapi.yaml](http://localhost:8083/docs/openapi.yaml)

## Переменные окружения

Проект использует переменные из `.env` файла. Ниже приведён перечень основных переменных:

| Переменная          | Назначение                          |
| ------------------- | ----------------------------------- |
| `POSTGRES_DB`       | Имя базы данных                     |
| `POSTGRES_USER`     | Пользователь базы данных            |
| `POSTGRES_PASSWORD` | Пароль пользователя                 |
| `JWT_SECRET_KEY`    | Секретный ключ для JWT токенов      |
| `ENCRYPTION_KEY`    | Ключ шифрования для хранения данных |

## Запуск модульных тестов

Для выполнения модульных тестов используется встроенный в Maven механизм:

```bash
mvn test
```