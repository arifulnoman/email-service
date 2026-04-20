# email-service

Centralized async email delivery microservice. Consumes `EmailRequestDTO` messages from RabbitMQ, dispatches via configurable SMTP accounts, and persists an audit log to PostgreSQL. SMTP accounts are cached in Redis.

---

## Stack

- **Spring Boot 4** · Java 17 · Maven
- **RabbitMQ** — async message queue with DLQ retry
- **PostgreSQL** — audit log (`email_logs`) + account store (`email_accounts`)
- **Redis** — SMTP account cache
- **Lombok**

---

## Running Locally

**Prerequisites:** Java 17+, PostgreSQL, RabbitMQ, Redis
Starts on **port 8085**.

---

## Key Configuration (`application.properties`)

| Property | Default |
|---|---|
| `server.port` | `8085` |
| `spring.rabbitmq.host` | `localhost` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/email_service_db` |
| `spring.data.redis.host` | `localhost` |
| `email.service.api-key` | _(set a strong secret)_ |

RabbitMQ names that **must match the producer**:

```properties
rabbitmq.exchange.email=email.exchange
rabbitmq.queue.email=email.queue
rabbitmq.routing-key.email=email.routing.key
```

---

## Email Account API

All endpoints require `X-Api-Key` header.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/email-accounts` | Create SMTP account |
| `GET` | `/api/email-accounts` | List all accounts |
| `GET` | `/api/email-accounts/{key}` | Get by key |
| `PUT` | `/api/email-accounts/{key}` | Update |
| `DELETE` | `/api/email-accounts/{key}` | Delete |

---

## Producer Payload

Publish to `email.exchange` with routing key `email.routing.key`:

```json
{
  "accountKey": "hrms-client",
  "to": "recipient@example.com",
  "cc": "optional@example.com",
  "subject": "Subject line",
  "templateName": "AUDIT_LABEL_ONLY",
  "body": "<html>...</html>",
  "inlineAttachments": [
    { "contentId": "logo", "contentType": "image/png", "data": "<base64>" }
  ]
}
```

> `body` must be the complete pre-rendered HTML. Template building is the producer's responsibility.

---

## Retry / DLQ

Failed messages are retried **3 times** with exponential backoff (2s → 4s → 8s), then routed to `email.dead-letter.queue`.
