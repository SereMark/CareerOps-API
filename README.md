# CareerOps API

[![CI](https://github.com/SereMark/CareerOps-API/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/SereMark/CareerOps-API/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

CareerOps API is a Java 21 / Spring Boot REST API for tracking a job search. It stores companies,
job postings, applications, follow-up tasks, interview rounds, offers, and a small dashboard in
PostgreSQL.

The code covers REST controllers, DTO validation, service-layer rules, transactions, Flyway
migrations, PostgreSQL constraints, OpenAPI docs, Docker Compose, CI, and automated tests.

All sample data is fictional. Real job-search notes should stay in a local database and should not
be committed to the repository.

## Start here

| Link | Why it is useful |
| --- | --- |
| [Architecture notes](docs/architecture.md) | Explains the package layout, domain rules, database constraints, transactions, and current scope. |
| [Five-minute demo](docs/demo.md) | Walks through the main workflow in Swagger UI. |
| [HTTP request collection](docs/careerops-api.http) | Lets you run the same workflow from an IDE REST client. |
| [PostgreSQL integration tests](src/test/java/com/seregergo/careerops/application/CareerOpsPostgreSqlIT.java) | Shows the workflow, rollback checks, optimistic locking, and Flyway schema running against PostgreSQL. |
| [CI workflow](.github/workflows/ci.yml) | Shows the GitHub Actions check used for the repository. |

## Stack

| Area | Technology |
| --- | --- |
| Runtime | Java 21, Spring Boot 3.5.16 |
| API | Spring Web, Bean Validation, `ProblemDetail`, springdoc-openapi / Swagger UI |
| Data | PostgreSQL 17, Spring Data JPA, Hibernate, Flyway |
| Tests | JUnit 5, Mockito, MockMvc, Testcontainers |
| Local setup and CI | Maven Wrapper, Docker Compose, GitHub Actions |

## Run it locally

You need Java 21 and Docker.

Start PostgreSQL:

```bash
docker compose up -d --wait
```

Start the application:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at `http://localhost:8080`.

| URL | Description |
| --- | --- |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Application and database health |

Flyway applies the database migrations at startup. Hibernate then checks that the JPA mappings match
the schema.

## Verify it

Run the fast local tests:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Run the full check with Docker running:

```bash
./mvnw verify
```

On Windows PowerShell:

```powershell
.\mvnw.cmd verify
```

`test` runs the unit, service, and controller tests. `verify` also runs the PostgreSQL
Testcontainers integration tests through Maven Failsafe, which is the same check used by CI.

## What the API models

CareerOps is built around a simple job-search workflow:

| Module | What it stores | Backend work shown |
| --- | --- | --- |
| `company` | Target companies, websites, notes, archive/restore state | Normalized names, uniqueness, validation, conflict responses |
| `jobposting` | Roles before applying, fit scores, salary range, triage priority | Request validation, scoring rules, filters, enum constraints |
| `application` | One application per posting, current status, status history | State transitions, transactions, audit events, optimistic locking |
| `nextaction` | Follow-up and preparation tasks | Due-date filters, completion state, timestamps |
| `interview` | Interview rounds, scheduling, format, outcome, notes | Child resources tied to an application |
| `offer` | Salary, benefits, hybrid policy, expiry, decision | One-offer-per-application rule and offer decision state |
| `dashboard` | Morning summary across the search | Read-only aggregation across repositories |

The scoring is simple by design. A job posting can be rated by Java/backend fit, mentoring,
salary, engineering practices, learning signal, and hybrid/life fit. A hard veto always makes the
posting `SKIP`, even if the score would otherwise be high.

## Application workflow

Every application starts in `SAVED`. The main happy path is:

```text
SAVED -> APPLIED -> SCREENING -> INTERVIEW -> OFFER -> ACCEPTED
```

Applications can also end in `REJECTED` or `WITHDRAWN`.

- `SAVED` can move to `APPLIED` or `WITHDRAWN`.
- `APPLIED`, `SCREENING`, `INTERVIEW`, and `OFFER` can move forward, or to `REJECTED` or
  `WITHDRAWN`.
- `ACCEPTED`, `REJECTED`, and `WITHDRAWN` are final.

Each job posting can have one application. When the status changes, the app updates the application
and writes a history event in the same transaction. If either write fails, both changes roll back.

## API overview

### Companies

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/companies` | Create a company |
| `GET` | `/api/companies` | List active companies |
| `GET` | `/api/companies?includeArchived=true` | Include archived companies |
| `GET` | `/api/companies/{id}` | Get a company |
| `PUT` | `/api/companies/{id}` | Update a company |
| `POST` | `/api/companies/{id}/archive` | Archive a company |
| `POST` | `/api/companies/{id}/restore` | Restore a company |

### Job postings

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/job-postings` | Create a job posting |
| `GET` | `/api/job-postings` | List job postings |
| `GET` | `/api/job-postings?companyId={id}` | Filter by company |
| `GET` | `/api/job-postings/{id}` | Get a job posting |
| `PUT` | `/api/job-postings/{id}` | Update a job posting |

Work mode can be `ONSITE`, `HYBRID`, or `REMOTE`. Target lane can be `JAVA_BACKEND`,
`JAVA_HEAVY_SOFTWARE_ENGINEER`, `GRADUATE_BACKEND`, or `OTHER`.

The triage score uses these fields:

| Field | Max |
| --- | ---: |
| `roleFitScore` | 30 |
| `mentoringScore` | 25 |
| `salaryScore` | 20 |
| `engineeringPracticesScore` | 10 |
| `learningSignalScore` | 10 |
| `hybridFitScore` | 5 |

Priority is based on the total: `PRIORITIZE` from 75, `APPLY` from 60, `MAYBE` from 45, and
`SKIP` below that or when a hard veto is present.

### Job applications

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/job-applications` | Create an application |
| `GET` | `/api/job-applications` | List applications |
| `GET` | `/api/job-applications?status={status}` | Filter by status |
| `GET` | `/api/job-applications/{id}` | Get an application |
| `POST` | `/api/job-applications/{id}/status` | Change its status |
| `GET` | `/api/job-applications/{id}/history` | View its status history |

The full request and response schemas are available in Swagger UI.

### Next actions

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/next-actions` | Create a follow-up or preparation task |
| `GET` | `/api/next-actions` | List open actions by due date |
| `GET` | `/api/next-actions?applicationId={id}` | List actions for one application |
| `GET` | `/api/next-actions?dueBefore=2026-07-05` | List open actions due by a date |
| `GET` | `/api/next-actions?completed=true` | List completed actions |
| `GET` | `/api/next-actions/{id}` | Get an action |
| `PUT` | `/api/next-actions/{id}` | Update an action |
| `POST` | `/api/next-actions/{id}/complete` | Mark an action complete |

### Interview rounds

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/interview-rounds` | Create an interview round |
| `GET` | `/api/interview-rounds` | List interview rounds |
| `GET` | `/api/interview-rounds?applicationId={id}` | List rounds for one application |
| `GET` | `/api/interview-rounds/{id}` | Get an interview round |
| `PUT` | `/api/interview-rounds/{id}` | Update an interview round |

### Offers

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/offers` | Create an offer |
| `GET` | `/api/offers` | List offers |
| `GET` | `/api/offers?applicationId={id}` | Get the offer for one application |
| `GET` | `/api/offers/{id}` | Get an offer |
| `PUT` | `/api/offers/{id}` | Update an offer |

### Dashboard

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/dashboard` | View status counts, due work, upcoming interviews, pending offers, and stale applications |

## Errors

API errors use `application/problem+json` and include a stable `errorCode`. Validation responses
also include errors grouped by field.

```json
{
  "type": "urn:careerops:problem:invalid-status-transition",
  "title": "Invalid application status transition",
  "status": 409,
  "detail": "Application status cannot move from SAVED to OFFER",
  "errorCode": "INVALID_STATUS_TRANSITION"
}
```

## Test layout

The project currently has 53 automated tests.

| Command | Runs | Docker required |
| --- | --- | --- |
| `./mvnw test` | Unit, service, and MockMvc controller tests | No |
| `./mvnw verify` | Fast tests plus PostgreSQL/Testcontainers integration tests | Yes |

The integration suite uses the `*IT` naming pattern and runs through Maven Failsafe. It covers the
Flyway-migrated PostgreSQL schema, the main HTTP workflow, transaction rollback, optimistic locking,
and job-search operations against the same database engine used by the application.

Docker must be running before `verify`; otherwise Testcontainers cannot start PostgreSQL.

## Configuration

The default settings match the included Docker Compose service:

| Variable | Default |
| --- | --- |
| `POSTGRES_DB` | `careerops` |
| `POSTGRES_USER` | `careerops` |
| `POSTGRES_PASSWORD` | `careerops` |
| `POSTGRES_PORT` | `5432` |

Copy `.env.example` to `.env` to change these values. To connect to another PostgreSQL instance, set
`DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

Stop the local database with:

```bash
docker compose down
```

Use this command if you also want to remove the stored database data:

```bash
docker compose down --volumes
```

## Documentation

- [Architecture notes](docs/architecture.md)
- [Five-minute demo](docs/demo.md)
- [HTTP request collection](docs/careerops-api.http)

## License

This project is available under the [MIT License](LICENSE).

## Author

Built by **Sere Gergő Márk**.
