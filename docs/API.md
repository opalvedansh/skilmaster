# SkillMate API

Base URL: `http://localhost:8080`. All endpoints except register/login/logout require the HttpOnly `sm_token` cookie.
Every POST/PUT/DELETE must send the header `X-Requested-With: XMLHttpRequest` (CSRF defence). Send requests with credentials.

Errors: `{ timestamp, status, error, message, fieldErrors? }`. Codes: 400 validation, 401 not logged in / expired, 403 forbidden, 404 not found (also used for other users' records), 409 conflict, 415 wrong Content-Type, 429 too many failed logins (5 per email, 15-minute lockout).

| Method | Path | Access | Notes |
|---|---|---|---|
| POST | /api/auth/register | public | `{name,email,password}`; always creates STUDENT; sets cookie; 201 |
| POST | /api/auth/login | public | `{email,password}`; sets cookie |
| POST | /api/auth/logout | public | clears cookie and revokes all of the user's tokens (every device); 204 |
| GET | /api/auth/me | any user | current user |
| GET | /api/users | ADMIN | `?q=&role=&page=&size=` paged |
| POST | /api/users | ADMIN | `{name,email,password,role}` |
| GET/PUT | /api/users/{id} | ADMIN or self | self may change name and password (needs `currentPassword`); admins may change email and role. A password change revokes that user's tokens; when you change your own, the response sets a fresh cookie |
| DELETE | /api/users/{id} | ADMIN | cannot delete self |
| GET | /api/subjects, /api/subjects/{id} | any | includes `skillCount`, caller's `progressPercentage` |
| POST/PUT/DELETE | /api/subjects[/{id}] | ADMIN | delete cascades to skills, questions, progress, attempts |
| GET | /api/subjects/{subjectId}/skills | any | |
| GET | /api/skills, /api/skills/{id} | any | `?subjectId=`; includes caller's status and percentage |
| POST/PUT/DELETE | /api/skills[/{id}] | ADMIN | `{name,subjectId}` |
| GET | /api/questions | any | `?skillId=&subjectId=&difficulty=&q=&page=&size=`; includes caller's `myStatus` |
| GET | /api/skills/{skillId}/questions | any | `?difficulty=` |
| GET | /api/questions/{id} | any | |
| POST/PUT/DELETE | /api/questions[/{id}] | ADMIN | `{questionText,difficulty,skillId}` |
| GET | /api/progress/me, /api/progress/me/skills/{skillId} | user | |
| POST | /api/progress | user | `{skillId,status}` upsert, one record per user+skill |
| PUT | /api/progress/{id} | owner | `{status}`; 404 if not yours |
| GET | /api/attempts/me, /api/attempts/me/question/{questionId} | user | `/me` returns newest first, `?limit=` default 100, max 500 |
| POST | /api/attempts | user | `{questionId,status: ATTEMPTED|SOLVED}`; identical retry within 10 s is not duplicated |
| GET | /api/projects/me | user | |
| POST/PUT/DELETE | /api/projects[/{id}] | owner | `{title,description,technology,githubUrl,status}` |
| GET | /api/dashboard/me, /me/progress, /me/readiness | user | |
| GET | /api/dashboard/admin | ADMIN | platform statistics |

## Business rules
- Progress: NOT_STARTED = 0%, COMPLETED = 100%, IN_PROGRESS = solved/total questions of the skill (clamped 1-99%, 50% if the skill has no questions). Recording an attempt moves NOT_STARTED to IN_PROGRESS and recalculates unless COMPLETED.
- Weak: started skill below 40%. Strong: 70% or more. Skills with no progress record (or NOT_STARTED) are "not assessed", never weak.
- Readiness: subject = average of its skills' percentages (missing = 0%); overall = average of subjects that have skills; `NOT_ASSESSED` when nothing has been started. It is an estimate, not a guarantee.
