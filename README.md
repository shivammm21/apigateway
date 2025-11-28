# Secure API Gateway (Spring Boot + Redis)

A lightweight API Gateway that provides JWT validation, per-user/IP rate limiting, IP blocking, request routing to microservices, analytics, recent logs, dynamic routing admin APIs, and health checks.

## Tech Stack
- Spring Boot 3 (Java 17)
- Spring Web, Spring Security, Actuator
- Spring Data Redis (Lettuce)
- Redis
- Docker + Docker Compose
- Lombok

## Run locally

### Prerequisites
- Java 17+
- Docker (optional but recommended)
- Redis (local or via docker)

### Option A: Run with Docker Compose
```
docker compose up --build
```
Services:
- Gateway: http://localhost:8767
- Redis: localhost:6379

### Option B: Run locally with local Redis
1) Start Redis (example):
```
docker run --rm -p 6379:6379 redis:alpine
```
2) Run the app:
```
./mvnw spring-boot:run
```

## Admin APIs

- GET `/auth/validate` — Validates JWT from `Authorization: Bearer <token>` header.
- GET `/admin/rate-limit/status?userId=<id>` — Current usage for a user.
- POST `/admin/rate-limit/update` — Update global limit.
  Body:
  ```json
  { "limitPerMinute": 200 }
  ```
- GET `/admin/ip-block/list` — List blocked IPs.
- POST `/admin/ip-block/block` — Block an IP.
  ```json
  { "ip": "123.45.67.89", "reason": "Suspicious activity" }
  ```
- POST `/admin/ip-block/unblock` — Unblock an IP.
  ```json
  { "ip": "123.45.67.89" }
  ```
- GET `/admin/analytics/traffic` — Traffic stats (by service/user).
- GET `/admin/analytics/errors` — Error stats (4xx/5xx/jwt/routing).
- GET `/admin/analytics/services` — Microservice statuses.
- GET `/admin/logs/recent` — Last 100 logs (JSON strings).
- GET `/admin/routes/list` — Current dynamic routes.
- POST `/admin/routes/add` — Add a new route.
  ```json
  { "path": "/payments/**", "serviceUrl": "http://payment-service:8083" }
  ```
- GET `/health` — Liveness endpoint.

## Dynamic Routing
Routes are stored in Redis hash `routes`: key is path pattern (e.g., `/users/**`), value is service base URL (e.g., `http://user-service:8081`). The gateway forwards any non-admin/non-internal requests by longest-prefix match.

Example to add routes:
```
curl -X POST http://localhost:8767/admin/routes/add \
  -H 'Content-Type: application/json' \
  -d '{"path":"/users/**","serviceUrl":"http://user-service:8081"}'
```

## JWT Validation
`AuthenticationFilter` performs a stubbed validation in `JwtService`. Replace with actual JWT verification (public key / secret) and parse claims to set `userId` and `role`. The `/auth/validate` endpoint exposes the same logic for services to query if needed.

## Rate Limiting
`RateLimitService` uses minute windows with Redis keys:
- `rate:user:{id}` and `rate:ip:{ip}` (with minute suffix)
- Global limit key: `rate:config:limitPerMinute` (default 200)

429 is returned when exceeded.

## IP Blocking
`IpBlockService` uses Redis set `block:ips`.

## Analytics
- Traffic: Redis hash `traffic:stats` (fields: `svc:<name>`, `user:<id>`)
- Errors: Redis hash `errors:stats` (fields include `jwt`, `routing`, etc.)
- Services status: Redis hash `services:status`

## Logs
Recent 100 logs are kept in Redis list `logs:recent`.

## Security
`SecurityConfig` currently permits `/admin/**`, `/auth/validate`, `/health`, `/actuator/**` for ease of testing. Lock these down as needed (e.g., with basic auth, IP allowlist, or OAuth2).

## Actuator
`/actuator` endpoints are enabled with detailed health for local operations. Review before production.

## Build
```
./mvnw -DskipTests package
```
Jar output: `target/apigateway-0.0.1-SNAPSHOT.jar`

## Folder Structure
See `src/main/java/com/apigateway/` for `config/`, `controller/`, `filter/`, `service/`, and `model/` packages.
