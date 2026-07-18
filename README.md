# Microstock Asset Management Platform

Digital asset management + submission tracking for microstock contributors. Modular monolith.

**Stack:** React + TypeScript + Vite + MUI (frontend) · Java 17 + Spring Boot 4 (backend) · PostgreSQL · MinIO/S3 · JWT auth with RBAC.

## Repository layout

```
backend/            Spring Boot API (package-by-feature)
frontend/           React + Vite app (added later)
docker-compose.yml  Postgres, MinIO, Mailpit
```

## Prerequisites

- Java 17 (Corretto), a running container engine (Rancher Desktop / Docker), Node 20+ (for the frontend).

## Running locally (Windows)

**Easiest — one command** (starts Docker infra + backend + frontend, each in its own window):

```powershell
.\start-dev.ps1     # then open http://localhost:5173
.\stop-dev.ps1      # stop the app servers (add -IncludeDocker to also stop Docker)
```

`start-dev.ps1` is idempotent (skips a server already running) and waits for Postgres
before launching. The two server windows run independently of the shell you launched from.

**Manual, if you prefer:**

```powershell
docker compose up -d                         # Postgres, MinIO, Mailpit
cd backend;  .\mvnw.cmd spring-boot:run       # backend  -> :8080  (Windows wrapper, NOT bash mvnw)
cd frontend; npm run dev                      # frontend -> :5173
```

Frontend: http://localhost:5173 (logs in through the Vite proxy) · Backend: http://localhost:8080
MinIO console: http://localhost:9001 (minioadmin/minioadmin) · Mailpit: http://localhost:8025

**Local dev admin:** the dev-profile seeder creates a local admin on first run and
prints its credentials once in the backend startup log. Or register a new account at
`/register`. In production the admin is created only from your `.env`
(`INITIAL_ADMIN_*`) — no default credentials exist.

## Auth API (implemented)

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/auth/register` | email + username + password (+confirm) |
| POST | `/api/auth/login` | by email **or** username |
| POST | `/api/auth/refresh` | rotating refresh tokens |
| POST | `/api/auth/logout` | revoke refresh token |
| POST | `/api/auth/change-password` | authenticated |
| GET  | `/api/auth/me` | current user |

## Database

Schema is owned by **Flyway** (`backend/src/main/resources/db/migration`). Hibernate is `validate`-only.
Public identifiers are UUIDs; media codes (`MED-000001`) are minted by a Postgres sequence.

## Tests

```powershell
cd backend
.\mvnw.cmd test    # boots a throwaway Postgres via Testcontainers
```
