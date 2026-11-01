# SkillMate

Skill-tracking and placement-preparation platform for college students.
React (Vite) frontend, Spring Boot 3 / Java 17 backend, MySQL 8 database.

```
skillMate/
  frontend/   React + Vite + React Router + Axios + Recharts
  backend/    Spring Boot, Spring Security (JWT), JPA, Flyway
  docs/API.md
```

## Prerequisites
Java 17+, Maven 3.9+, Node 18+, MySQL 8.

## 1. Create the database
```sql
CREATE DATABASE skillmate CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'skillmate_user'@'localhost' IDENTIFIED BY 'choose-a-password';
GRANT ALL PRIVILEGES ON skillmate.* TO 'skillmate_user'@'localhost';
```
Flyway creates the tables on first start (`V1__create_schema.sql`) and loads demo subjects, skills and questions (`V2__seed_reference_data.sql`, reference content only: no users, progress or attempts).

## 2. Run the backend
```bash
cd backend
cp .env.example .env      # fill in DB credentials, JWT_SECRET (32+ chars) and the first admin login
mvn spring-boot:run       # http://localhost:8080
```
`.env` is read automatically from the `backend` folder; real environment variables work too. No secrets are in source code.
If `SKILLMATE_ADMIN_EMAIL` and `SKILLMATE_ADMIN_PASSWORD` are set, that ADMIN account is created at startup if missing. Public registration can only create STUDENT accounts; admins can promote users on the Users page.

## 3. Run the frontend
```bash
cd frontend
cp .env.example .env      # VITE_API_BASE_URL=http://localhost:8080
npm install
npm run dev               # http://localhost:5173
```

## Tests
```bash
cd backend && mvn test      # JUnit + MockMvc on in-memory H2 (MySQL mode), no MySQL needed
cd frontend && npm test     # Vitest + Testing Library
```

## Security design
- Passwords are stored as BCrypt hashes. Identity always comes from the validated token, never from IDs sent by the client.
- Login sets a JWT in an **HttpOnly, SameSite=Lax cookie** (`sm_token`, default lifetime 120 min, set `COOKIE_SECURE=true` behind HTTPS), so scripts cannot read the token. Nothing is kept in localStorage.
- CSRF: every non-GET request must carry `X-Requested-With`; together with SameSite and a restricted CORS origin list this blocks cross-site requests.
- Roles are enforced on the server (`@PreAuthorize`, URL rules). Other users' progress, attempts and projects return 404.
- Frontend and backend must be on the same site (for example localhost with different ports, or one domain behind a reverse proxy) for the SameSite cookie to be sent.
- Expired or invalid tokens give 401; the frontend then clears its state and asks the user to log in again.

## Implemented features
Landing, registration, login/logout, student dashboard with charts, subjects, skills with status updates, practice questions with filters and persisted attempts, progress page, placement-readiness page, projects CRUD, profile (name and password), admin dashboard, admin CRUD with search, filters, pagination and delete confirmation for users, subjects, skills and questions. Formulas are documented in `docs/API.md`.

## Known limitations
- Subject and skill admin lists page on the client (the APIs return full lists); users and questions page on the server.
- Deleting a subject or skill cascades to questions, progress and attempts (the confirmation dialog says so).
- Readiness uses progress data only; question accuracy and projects are not part of the formula (the Projects subject can be configured like any other).
- Email verification, password reset and refresh tokens are not implemented.
- The code was written without network access in the authoring environment, so `mvn test` and `npm test` should be run once after unpacking to confirm a clean build.
