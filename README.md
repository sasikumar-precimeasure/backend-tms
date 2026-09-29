# tms-backend

Spring Boot backend for the TMS (Temperature Monitoring System) frontend.
Handles authentication/user management, stores IRTCC and 2243 device
readings pushed from the frontend every 60 seconds, keeps an audit log of
annunciation acks / AVR mode changes / AVR Settings edits / mail
configuration changes, and sends threshold-based alert emails.

Built clean-architecture style (`domain` -> `application` -> `infrastructure`)
so this can be run standalone, independently on each client's own premises -
no shared/cloud component, no multi-tenancy inside the app. Each install is
one backend + one PostgreSQL database, entirely owned by that client.

## Requirements

- Java 17+
- PostgreSQL 13+ (a fresh, empty database - Flyway creates the schema on
  first boot)

## Configuration

All environment-specific settings are environment variables (see
`.env.example` for the full list and defaults). At minimum, before any real
deployment, set:

- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` - this client's own Postgres.
- `JWT_SECRET` - a real per-install secret (`openssl rand -base64 32`).
- `MAIL_ENCRYPTION_SECRET` - a real per-install secret. Keep this stable
  once set; rotating it makes any already-saved SMTP password in Mail
  Configuration unreadable (re-enter it there after rotating).
- `ADMIN_SEED_PASSWORD` - the initial super-admin login's password. Change
  it via the app immediately after first login in a real deployment.
- `CORS_ALLOWED_ORIGINS` - the frontend's own origin.

## Running locally

```bash
# Start a local Postgres (or point DB_URL at an existing one):
docker run -d --name tms-postgres -e POSTGRES_USER=tms -e POSTGRES_PASSWORD=tms -e POSTGRES_DB=tms -p 5432:5432 postgres:16

./gradlew bootRun
```

On first boot, Flyway applies `src/main/resources/db/migration/V1__init.sql`
against the empty database, and `AdminSeeder` creates the initial
super-admin login (`ADMIN_SEED_USERNAME`/`ADMIN_SEED_PASSWORD`, default
`admin` / `admin123` for local dev only).

## Running via Docker

```bash
docker build -t tms-backend .
docker run -p 8080:8080 --env-file .env tms-backend
```

Containers are optional - the packaged jar (`./gradlew bootJar`, then
`java -jar build/libs/tms-backend-*.jar`) runs identically on any server
with a JVM and no container runtime, for clients whose premises don't
support Docker.

## API contract

Base path `/tms/api`. Matches the existing TMS frontend's expected contract
exactly (see `tms/src/infrastructure/repositories/AuthRepositoryImpl.ts` and
`tms/src/infrastructure/api/client.ts`):

- `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`,
  `POST /auth/forgot-password`, `POST /auth/reset-password`, `GET /users/me`
- `GET/POST /users`, `PATCH /users/{id}`, `PATCH /users/{id}/status` -
  super-admin user management (gated on the caller's own "Users" menu
  write permission)
- `GET/POST /roles`, `PUT /roles/{id}/permissions`
- `POST /readings/batch` - the frontend's 60-second reading push
- `POST /audit-events`, `GET /audit-events`, `GET /audit-events/device/{id}`
- `GET/PUT /mail-settings/sender`, `GET/POST/DELETE /mail-settings/recipients`,
  `GET/PUT /mail-settings/thresholds`

## Testing

```bash
./gradlew test
```

`EvaluateMailThresholdsUseCaseTest` covers the scheduled mail-threshold job
(threshold comparison + re-alert-interval suppression) against in-memory
fakes, no real database or SMTP server required.
