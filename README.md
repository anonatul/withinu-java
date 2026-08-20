# WithinU

Anonymous campus chat. A location check proves you're on campus; your identity never does.

Users inside a defined campus polygon get an anonymous identity and can talk in moderated
rooms. The geospatial check runs server-side with PostGIS — the client can't claim to be
on campus.

## Stack

- Backend: Java 21, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA (hibernate-spatial), Flyway
- DB: PostgreSQL 16 + PostGIS (SRID 4326)
- Rate limiting / geo cache: Redis (Lua)
- Frontend: React 19, TypeScript, Vite, TanStack Query, Tailwind
- Infra: Docker Compose, Testcontainers

## Quick start

```bash
cp .env.example .env    # set a real JWT_SECRET
docker compose up --build
```

| Service | URL |
| --- | --- |
| Frontend | http://localhost:3000 |
| API + Swagger | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/api/v1/health |

Flyway creates the schema and seeds data. Default admin login: `admin` / `admin123`
(change before deploying anywhere real).

Seeded campus boundary: TCET, Kandivali (E), Mumbai (~19.2065°N, 72.8748°E).

### Configuration

| Variable | Default | Notes |
| --- | --- | --- |
| `JWT_SECRET` | dev-only secret | use ≥32 random bytes |
| `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` | localhost / withinu | JDBC URL |
| `REDIS_HOST` / `REDIS_PORT` | localhost / 6379 | |
| `CORS_ALLOWED_ORIGINS` | http://localhost:5173,http://localhost:3000 | comma-separated |
| `CAMPUS_BOUNDARY_WKT` | empty | WKT polygon, SRID 4326; overrides seed at startup |
| `ALLOW_ANY_LOCATION` | false | dev only — skips the geo check entirely |

## API

Base: `/api/v1`. JWT for everything except geo token and admin login.

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/token/geo` | — | Verify lat/lon against boundary, issue anonymous JWT |
| GET | `/health` | — | Liveness + DB/Redis |
| GET | `/rooms` | user | Active rooms with counts |
| GET | `/rooms/{id}` | user | Single room |
| GET | `/messages?roomId=&page=&size=` | user | Paginated, newest first |
| POST | `/messages` | user | Send (1–1000 chars, 10/min) |
| DELETE | `/messages/{id}` | user | Soft-delete own message |
| POST | `/reports` | user | Report: SPAM, HARASSMENT, ABUSE, ILLEGAL_CONTENT, OTHER |
| POST | `/admin/login` | — | Admin JWT |
| GET/POST/PATCH/DELETE | `/admin/rooms` | admin | Room management |
| GET/DELETE | `/admin/messages` | admin | Moderation |
| GET/PATCH | `/admin/reports` | admin | Resolve / dismiss |

Errors are `{ "success": false, "errorCode": "...", "message": "..." }`.

## Geo check

```sql
SELECT EXISTS (
  SELECT 1 FROM campus_boundaries cb
  WHERE cb.active = true
    AND ST_Contains(cb.boundary, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
)
```

The coordinate is never stored — only the inside/outside result, cached in Redis for 5 min.

## Tests

Integration tests run against real PostGIS + Redis via Testcontainers:

```bash
cd backend && ./mvnw test
```

## Layout

```text
backend/    Spring Boot API (controllers → services → repositories)
frontend/   React app (Vite)
docker-compose.yml   postgis + redis + backend + frontend
```