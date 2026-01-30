# cogniq-notification-service

Multi-channel **Communication Hub** for CogniQ Flow: email delivery, real-time WebSocket alerts, and dashboard notification history.

## Role

- **Decouple** business logic from delivery logic.
- **Channels**: Email (SMTP / SendGrid / SES), WebSocket (STOMP) for in-app toasts, and persistent **Dashboard Inbox** (notification log).

## Architecture

- **Eureka**: Registers as `cogniq-notification-service`.
- **Gateway**: `/api/notifications/**` → notification service (e.g. `/inbox` for dashboard). **Internal** `/internal/send` is **not** exposed via Gateway; other services call it via service discovery.
- **Security**: JWT for `/inbox` and WebSocket; `/internal/**` permitted (internal only).
- **Database**: PostgreSQL, Flyway migrations, `notification_templates` and `notification_log` tables.
- **Actuator** & **Prometheus**: Health, metrics.
- **Swagger**: `/swagger-ui.html`, `/v3/api-docs`.

## API

### Internal (other microservices)

- **POST /internal/send**  
  Body: `{ "orgId": "uuid", "recipientId": "uuid", "recipientEmail": "user@example.com", "templateId": "BILLING_SUCCESS", "channels": ["EMAIL", "WEB_SOCKET"], "params": { "amount": "$49.00", "credits": "500" } }`  
  No Gateway route; call via `lb://cogniq-notification-service/internal/send`.

### Public (via Gateway, JWT required)

- **GET /inbox?page=0&size=20**  
  Paginated notification history for the authenticated user.

### WebSocket

- **Endpoint**: `/ws` (SockJS + STOMP).
- **Subscribe**: `/topic/user-{userId}/alerts` — user subscribes to their own topic (JWT-secure in production).

## Config

- **application.yml** / **application-dev.yml** / **application-prod.yml**: port 8085, DB `cogniq_notification`, `spring.mail.*`, `cogniq.notification.from-email`, `cogniq.jwt.secret`.
- **Prod**: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `MAIL_*`, `JWT_SECRET`, `NOTIFICATION_FROM_EMAIL`.

## Templates

Seeded in Flyway `V2__seed_notification_templates.sql`: `VERIFICATION_CODE`, `PASSWORD_RESET`, `BILLING_SUCCESS`, `CREDIT_LOW`, `TRAINING_STARTED`, `TRAINING_FINISHED`, `LARGE_CSV_PROCESSED`. Placeholders: `{{name}}`, `{{code}}`, `{{link}}`, `{{amount}}`, `{{credits}}`, `{{fileName}}`, etc.

## Run

1. Create DB: `createdb cogniq_notification`
2. Start Eureka, then: `mvn spring-boot:run` (profile `dev`).
3. Gateway routes `/api/notifications/**` to this service; internal callers use Feign or RestTemplate to `http://cogniq-notification-service:8085/internal/send`.
