# Distributed URL Shortener

A Java 17 / Spring Boot 3 URL shortener with PostgreSQL persistence, Redis cache-aside redirects, a Redis-backed token bucket, and asynchronous click tracking.

## Run locally

```bash
docker compose up --build
```

Create a link (the API key is required and is rate limited to 100 requests per minute by default):

```bash
curl -X POST http://localhost:8080/api/urls \
  -H 'Content-Type: application/json' -H 'X-API-Key: demo-key' \
  -d '{"longUrl":"https://example.com"}'
```

Open the returned `shortUrl`, or get its statistics:

```bash
curl http://localhost:8080/api/urls/<shortCode>/stats
curl http://localhost:8080/actuator/health
```

## Design notes

- **Collision-proof codes:** PostgreSQL allocates an ID before the mapping is inserted; its Base62 representation becomes the code.
- **Fast redirects:** Redis holds `shortCode → longUrl` for one hour. A miss reads PostgreSQL then repopulates Redis (cache-aside).
- **Abuse protection:** an atomic Lua script implements a per-key token bucket in Redis, using Redis server time so app instances agree.
- **Click analytics:** the redirect responds immediately, while an `@Async` transaction increments the database count.

## Tests

```bash
mvn verify
```

Unit tests cover Base62 encoding and a Mockito cache-hit path. The integration test uses real PostgreSQL and Redis via Testcontainers, and is skipped automatically on machines without Docker.
