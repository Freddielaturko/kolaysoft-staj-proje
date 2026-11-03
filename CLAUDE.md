# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Weekly project status reporting system (Kolaysoft internship project) that replaces PowerPoint-based reporting. PMs submit weekly progress per project, the CTO views a filtered portfolio dashboard, and the Admin manages users and projects. The domain language is Turkish: entity fields, DTO fields, enum values, error messages and docs are all in Turkish (e.g. `raporHaftasi`, `genelDurum`, `riskSeviyesi`, `sorumluPm`, `sifre`). Keep new code consistent with that. Code comments are written in ASCII Turkish (no diacritics).

## Commands

Backend (`backend/`, Java 25, Spring Boot 4.1, Maven):
```bash
cp .env.example .env          # loaded automatically via springboot4-dotenv
mvn spring-boot:run           # API on http://localhost:8080, needs PostgreSQL (DB_URL in .env)
mvn package                   # build jar
```

Frontend (`frontend/`, React 18 + Vite 5):
```bash
cp .env.example .env          # VITE_API_BASE_URL
npm install
npm run dev                   # http://localhost:5173
npm run build
```

Tests: there are no automated tests (no `src/test`, no JS tests), although the pom already includes `spring-boot-starter-test`, `spring-security-test` and H2. Manual scenarios live in `docs/test-senaryolari.md`. `docs/negatif-testler.ps1` is a PowerShell script that checks negative API cases (409, 400, 401 and so on) against a local backend. It asks for admin credentials through `Read-Host`, so it must be run interactively. `npm run lint` is defined, but the repo has no ESLint config.

## Architecture

**Backend** (`com.kolaysoft.projedurum`):
- Controllers talk to repositories directly; there is no service layer apart from `ProjectLookupService.findOr404`. Business rules such as the weekly-report upsert and the CTO dashboard filtering live in the controllers.
- DTOs are Java records. Each response DTO has a static `from(entity)` mapper.
- Errors are raised as `ResponseStatusException` and turned into JSON by `GlobalExceptionHandler`. Validation errors return 400 with an `alanlar` field map.
- Schema is managed by Hibernate `ddl-auto: update`. There are no migrations, so changing an entity changes the DB.
- `AdminSeeder` creates the first admin from `APP_ADMIN_*` env vars when the `users` table is empty. There is no public registration.

**Authorization** is layered, and you need to read three places together to see it:
1. `SecurityConfig`: `/api/auth/**` is public, `/api/admin/**` requires ADMIN, and everything else needs a JWT. Unauthenticated requests get 401 instead of Spring's default 403.
2. `@PreAuthorize` on specific endpoints (e.g. `GET /api/dashboard/cto` is CTO only).
3. `ProjectAccessGuard.ensureCanAccess` / `ensureCanWrite`, called manually inside controllers for per-project rules: a PM can only access projects where they are `sorumluPm`, and the CTO is read-only. Any new project-scoped endpoint must call the guard.

Stateless JWT: `JwtAuthFilter` → `JwtService`, and the principal is `UserPrincipal` (wrapping `User`, roles ADMIN/PM/CTO).

**Weekly reports**: one report per (project, `raporHaftasi`), enforced by a unique constraint. POST upserts on that pair. The CTO dashboard takes the latest report per project (`findLatestReportPerProject`) and filters it in memory.

**Frontend**: `api/client.js` is a shared axios instance that attaches the token from `localStorage` and, on any 401, clears storage and hard-redirects to `/login`. Per-resource API modules sit in `src/api/`. `AuthContext` holds the user, and `App.jsx` routes each role to its single home page (`/admin`, `/pm`, `/cto`) through `ProtectedRoute`. Every page has its own `.css` file next to it.

## Deployment

Frontend is on Vercel (root `frontend`, `vercel.json` handles SPA rewrites). Backend is on Render as Docker (Dockerfile path `backend/Dockerfile`, build context `backend`), with Render PostgreSQL. See `docs/deployment.md`. Gotchas already hit:
- Lombok has to stay explicitly listed in `maven-compiler-plugin` `annotationProcessorPaths`, because JDK 23+ disabled implicit processor discovery.
- The Dockerfile downloads Maven from `archive.apache.org`, not `dlcdn`.
- `CORS_ALLOWED_ORIGINS` is a comma-separated list.
