# Secure API Gateway — Docker Quickstart

A lightweight API Gateway (Spring Boot) with JWT validation, per-user/IP rate limiting, IP blocking, dynamic routing, analytics, recent logs, and health endpoints. Redis is used for rate limits, analytics, IP blocks, recent logs, and the dynamic route table.

Image: `shivammm21/apigateway:latest`

- Container internal port: `8767` (map any host port you prefer)
- Requires: a reachable Redis instance (container or host)

---

## 1) Pull the image
```bash
docker pull shivammm21/apigateway:latest
```

## 2) Start Redis (required)
The gateway needs Redis for core features. Start Redis on your host (or in Docker) before running the gateway:
```bash
docker run -d --name redis -p 6379:6379 redis:alpine
```

What uses Redis:
- Rate limiting
- Analytics
- IP blocking
- Logs
- Dynamic route table

> Tip: If 6379 is already in use on your host, use another host port, e.g. `-p 6380:6379`.

## 3) Run the API Gateway container
Map any host port (example: 7678) to the container's internal port 8767. Point the gateway to your host Redis using `host.docker.internal`.

```bash
docker run -d --name api-gateway \
  -p 7678:8767 \
  -e REDIS_HOST=host.docker.internal \
  shivammm21/apigateway:latest
```

Why this works:
- The gateway listens on container port 8767.
- It is exposed on your host as 7678 (or any host port you choose).
- Redis is running on the host at 127.0.0.1:6379; the container reaches it via `host.docker.internal`.

> Linux note: `host.docker.internal` is supported on recent Docker versions. If it does not resolve, add `--add-host=host.docker.internal:host-gateway` to the `docker run` command.

## 4) Add routes (important)
The gateway forwards requests based on the route table stored in Redis.
Assume your backend service runs at `http://localhost:8081` on the host. Add a route:

```bash
curl -X POST http://localhost:7678/admin/routes/add \
  -H "Content-Type: application/json" \
  -d '{"path":"/users/**","serviceUrl":"http://host.docker.internal:8081"}'
```

This tells the gateway: any request matching `/users/**` should be forwarded to the backend on 8081.

## 5) Call your APIs through the gateway
Before (direct to backend):
```bash
GET http://localhost:8081/users/1
```

After (via gateway):
```bash
GET http://localhost:7678/users/1
Authorization: Bearer <jwt>
```

The gateway will:
- Check IP block
- Check rate limit
- Validate JWT (pluggable; dev stub by default)
- Log request
- Forward to your service

## 6) Useful admin endpoints
- Validate JWT:
```bash
curl -H "Authorization: Bearer sometokenvalue12345" \
  http://localhost:7678/auth/validate
```

- Rate limiting:
```bash
# Check usage
curl 'http://localhost:7678/admin/rate-limit/status?userId=123'

# Update global limit
curl -X POST http://localhost:7678/admin/rate-limit/update \
  -H 'Content-Type: application/json' \
  -d '{"limitPerMinute": 200}'
```

- IP Blocking:
```bash
# Block an IP
curl -X POST http://localhost:7678/admin/ip-block/block \
  -H 'Content-Type: application/json' \
  -d '{"ip":"1.2.3.4","reason":"Suspicious"}'
```

- Analytics:
```bash
curl http://localhost:7678/admin/analytics/traffic
curl http://localhost:7678/admin/analytics/errors
```

- Recent logs:
```bash
curl http://localhost:7678/admin/logs/recent
```

## 7) Health check
```bash
curl http://localhost:7678/health
```
Expected response:
```json
{"status":"UP"}
```

---

## Alternative: run both gateway and Redis with Docker Compose
If you prefer everything inside containers and isolated from host ports, use Compose:

```yaml
version: "3.8"
services:
  redis:
    image: redis:alpine
    container_name: apigateway-redis
    command: ["redis-server", "--appendonly", "yes"]
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    restart: unless-stopped

  gateway:
    image: shivammm21/apigateway:latest
    ports:
      - "7678:8767"   # host:container
    environment:
      - REDIS_HOST=redis
    depends_on:
      redis:
        condition: service_healthy
    restart: unless-stopped

volumes:
  redis-data:
```

```bash
docker compose up -d
curl http://localhost:7678/health
```

---

## Environment variables
- `REDIS_HOST` (required): Hostname of Redis. Examples:
  - `host.docker.internal` (Redis on host)
  - `redis` (Redis service name in the same Docker network/Compose project)
- `JAVA_OPTS` (optional): JVM flags, e.g. `-Xms256m -Xmx512m`.

## Troubleshooting
- Port already in use:
  - Change host port mapping, e.g. `-p 8088:8767`.
- `ECONNREFUSED` or `Connection refused` to Redis:
  - Ensure Redis is running and reachable from the container.
  - If using host Redis on Linux, add `--add-host=host.docker.internal:host-gateway` to `docker run`.
- 500 on `/health`:
  - Verify that Redis is up; some components may query Redis. The provided `/health` endpoint should respond even when routing is disabled.
- Inspect logs:
  ```bash
  docker logs --tail=200 api-gateway
  ```

## Clean up
```bash
docker rm -f api-gateway redis 2>/dev/null || true
```

---

## Summary
1) Pull the image
2) Start Redis
3) Run the gateway (map a host port to 8767 and set `REDIS_HOST`)
4) Add routes to your backend services
5) Call your APIs via the gateway and use the admin endpoints for validation, rate-limiting, IP blocking, analytics, and logs
