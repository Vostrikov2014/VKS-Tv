# Jitsi Management Platform (JMP)

Централизованная платформа для администрирования, мониторинга и управления видеоконференциями на базе Jitsi Meet.

## 📋 Описание

JMP предоставляет единый веб-интерфейс для управления жизненным циклом видеоконференций Jitsi с поддержкой мультитенантности, ролевой модели доступа и комплексного аудита.

## 🏗️ Архитектура

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.2+**
- **Spring Security 6** с JWT аутентификацией
- **Spring Data JPA** + Hibernate 6.4+
- **PostgreSQL 16** для хранения данных
- **Redis 7** для кэширования и сессий
- **Flyway** для миграций БД
- **MapStruct** для маппинга DTO
- **Resilience4j** для circuit breaker
- **Micrometer** + Prometheus для метрик
- **OpenTelemetry** для распределённой трассировки

### Frontend
- **React 18/19**
- **TypeScript 5+**
- **Vite** для сборки
- **Material UI (MUI) v5**
- **Zustand** для управления состоянием
- **React Router v6**
- **Axios** для HTTP запросов

### Инфраструктура
- **Docker** + Docker Compose
- **Nginx** как reverse proxy
- **Grafana** + Prometheus для мониторинга
- **RabbitMQ** для асинхронной обработки webhook'ов

## 🚀 Быстрый старт

### Требования
- Java 21+
- Node.js 18+
- Docker + Docker Compose
- PostgreSQL 16+
- Redis 7+

### Запуск через Docker Compose

```bash
cd infra
docker-compose up -d
```

### Локальная разработка

#### Backend
```bash
cd jmp-backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd jmp-frontend
npm install
npm run dev
```

## 📁 Структура проекта

```
/workspace
├── jmp-backend/              # Spring Boot backend
│   ├── src/main/java/com/jmp/
│   │   ├── domain/          # Domain entities (DDD)
│   │   ├── application/     # Application services
│   │   ├── presentation/    # REST controllers
│   │   └── infrastructure/  # Security, JPA config
│   └── src/main/resources/
│       ├── db/migration/    # Flyway миграции
│       └── application.yml  # Конфигурация
├── jmp-frontend/            # React frontend
│   └── src/
│       ├── components/      # React компоненты
│       ├── pages/           # Страницы приложения
│       ├── services/        # API клиенты
│       └── store/           # State management
└── infra/                   # Docker compose, nginx
```

## 🔐 Безопасность

- JWT access токены (15 мин) + refresh токены (7 дней)
- BCrypt хеширование паролей (cost factor >= 10)
- RBAC модель доступа (Super Admin, Tenant Admin, Moderator, User, Auditor)
- Rate limiting через Redis
- Аудит всех действий (GDPR/152-FZ compliance)
- Шифрование записей AES-256-GCM

## 📊 Основные возможности

### Управление пользователями
- Регистрация и верификация email
- Двухфакторная аутентификация (TOTP)
- Управление ролями и правами доступа
- Блокировка после неудачных попыток входа

### Мультитенантность
- Изоляция данных на уровне tenant
- Квоты на участников, длительность, записи
- Индивидуальные настройки Jitsi домена

### Конференции
- Создание и планирование комнат
- Генерация JWT токенов для доступа
- Управление участниками в реальном времени
- Настройка функций (запись, чат, демонстрация экрана)

### Записи
- Интеграция с Jibri для записи
- Хранение в S3 с шифрованием
- Политики хранения (retention policies)
- Поиск и воспроизведение записей

### Мониторинг
- Real-time статус конференций
- Метрики JVB/Jicofo узлов
- Графики нагрузки и производительности
- Уведомления о событиях

### Аудит
- Полная история действий администраторов
- Поиск и фильтрация логов
- Экспорт для compliance отчётности

## 📖 API Документация

После запуска backend Swagger UI доступен по адресу:
```
http://localhost:8080/api/v1/swagger-ui.html
```

OpenAPI спецификация:
```
http://localhost:8080/api/v1/v3/api-docs
```

## 🧪 Тестирование

### Backend тесты
```bash
cd jmp-backend
./mvnw test
./mvnw verify  # С отчётом JaCoCo
```

### Frontend тесты
```bash
cd jmp-frontend
npm test
npm run test:e2e  # E2E тесты Playwright
```

## 📈 Мониторинг

### Prometheus метрики
```
http://localhost:8080/api/v1/actuator/prometheus
```

### Health checks
```
http://localhost:8080/api/v1/actuator/health
http://localhost:8080/api/v1/actuator/info
```

### Grafana дашборды
```
http://localhost:3000
```

## 🔧 Конфигурация

### Переменные окружения (Backend)

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `SPRING_PROFILES_ACTIVE` | Профиль Spring | `dev` |
| `DB_HOST` | PostgreSQL хост | `localhost` |
| `DB_PORT` | PostgreSQL порт | `5432` |
| `DB_NAME` | Имя БД | `jmp` |
| `DB_USER` | Пользователь БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `postgres` |
| `REDIS_HOST` | Redis хост | `localhost` |
| `REDIS_PORT` | Redis порт | `6379` |
| `JWT_SECRET` | Секрет JWT (мин. 32 символа) | - |
| `JITSI_DOMAIN` | Домен Jitsi | `meet.jitsi` |
| `JITSI_API_URL` | URL Jitsi API | `http://localhost:8778` |
| `S3_BUCKET` | S3 бакет для записей | `jmp-recordings` |

### Переменные окружения (Frontend)

Создайте файл `.env` в директории `jmp-frontend`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/api/v1/ws
```

## 📝 Лицензия

Apache 2.0

## 👥 Команда

- Архитектор: JMP Team
- Tech Lead: JMP Team
- Разработка: JMP Team

## 📚 Документация

- [Jitsi Documentation](https://jitsi.github.io/handbook/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Docs](https://react.dev/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)

## 🚧 Roadmap

### Phase 1 (MVP) ✅
- [x] Аутентификация и авторизация
- [x] Базовое управление комнатами
- [x] Генерация JWT токенов
- [x] Интеграция Jitsi Webhooks
- [x] React Dashboard

### Phase 2 (Core) 🚧
- [ ] Мультитенантность
- [ ] Ролевая модель
- [ ] Управление записями
- [ ] Real-time мониторинг
- [ ] Аудит действий

### Phase 3 (Advanced)
- [ ] Аналитика и отчёты
- [ ] Планировщик конференций
- [ ] Интеграция с календарями
- [ ] Feature flags

### Phase 4 (Enterprise)
- [ ] SSO/OIDC
- [ ] Compliance отчётность
- [ ] Disaster recovery
- [ ] Multi-region deployment

---

**Версия спецификации:** 1.0.0  
**Последнее обновление:** 2026-04-05
