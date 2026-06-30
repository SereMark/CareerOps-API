# Architecture

CareerOps is a modular monolith. I kept the structure simple on purpose: each feature has its own
package, and the shared pieces stay small.

The main packages are company, job posting, application, next action, interview, offer, and
dashboard. Shared validation and error handling live outside those feature packages.

```text
company
    -> job posting
        -> job application
            -> status event
            -> next action
            -> interview round
            -> offer
dashboard
    -> read-only summary across the job-search records
```

Most requests follow the normal Spring path:

```text
controller -> service -> repository -> PostgreSQL
```

- Controllers define the HTTP API and validate request bodies.
- Services handle business rules, text cleanup, and transactions.
- Repositories load and save entities.
- Flyway owns the database schema.
- Hibernate runs in validation mode, so startup fails if the mappings and schema drift apart.

## Domain model

### Companies and job postings

A company can have many job postings. Companies are archived instead of deleted so related postings
and applications can still be read later. Company names are stored with a normalized lowercase value
and must be unique.

A posting belongs to one company. It stores the role title, source URL, location, work mode, notes,
and a few fields used before applying: target lane, seniority, salary range, fit scores, and an
optional hard veto reason.

The score is simple by design:

```text
role fit 30 + mentoring 25 + salary 20 + engineering practices 10
    + learning signal 10 + hybrid/life fit 5 = 100
```

The API returns a priority from that score. A hard veto always returns `SKIP`, because one serious
problem should outweigh a high numeric score.

### Job applications

A posting can have one application. Keeping postings and applications separate lets the API store
roles that were considered but never applied to.

The one-application rule is checked in the service and backed by a unique database constraint. The
service gives normal users a clear conflict response, and PostgreSQL still protects the data if two
requests try to create the same application at once.

## Status transitions

`ApplicationStatus` contains the allowed moves:

```text
SAVED -> APPLIED -> SCREENING -> INTERVIEW -> OFFER -> ACCEPTED
```

Applications can also be rejected or withdrawn where that makes sense. Accepted, rejected, and
withdrawn applications cannot be reopened.

Keeping the transition rules in the enum makes the policy visible in one place and keeps the
controller layer thin.

## Transactions and history

Creating an application also creates the first `SAVED` history event. Later status changes update
the application and append a new event with the old status, new status, timestamp, and optional
note.

Both writes happen in one service transaction. If the event write fails, the status update rolls
back too, so the current status cannot get ahead of its history.

Status events have no update or delete endpoints. They form an append-only timeline for the
application.

## Job-search operations

Next actions, interview rounds, and offers hang from an application instead of being squeezed into
the status field.

- Next actions are small to-do items with a type, due date, completion timestamp, and notes.
- Interview rounds track scheduling, format, outcome, preparation notes, questions asked, and
  follow-up timing.
- Offers track gross monthly salary, benefits, hybrid policy, review promise, expiry, decision, and
  decision notes.

This keeps the application status focused on the pipeline stage, while the extra resources store
the work around that stage.

## Dashboard

The dashboard is read-only. It gathers the records I would want to check at the start of a job
search day:

- application counts by status;
- active applications;
- open and overdue next actions;
- scheduled interviews in the next seven days;
- pending offers;
- active applications that have not changed for seven days.

It is implemented as a service over the existing repositories. There is no separate reporting
database because that would add more moving parts than this project needs.

## Concurrent updates

Applications use a JPA `@Version` field. If two requests start from the same version, the first one
may complete and the second one receives `409 Conflict`. That prevents competing status changes
from writing a confusing history.

The status-event table also has a unique constraint on `(application_id, new_status)`. It is a
second guard against duplicate target statuses in one application's history.

## Database constraints

PostgreSQL enforces the main data rules:

- unique normalized company names;
- one application per job posting;
- valid work-mode, application-status, triage, task, interview, and offer values;
- bounded triage scores and valid salary ranges;
- one offer per application;
- no duplicate target status in one application's history;
- foreign keys between related records;
- optimistic locking through the application version column.

The integration tests run the Flyway migrations against PostgreSQL with Testcontainers. They cover
the main HTTP workflow, job-search operations, rollback behavior, and stale updates using the same
database engine as the app.

## Current scope

This version focuses on backend workflow modeling. It does not include authentication, pagination,
a frontend, or deployment-specific configuration.

Those are conscious scope choices for now. The current code stays focused while still showing
controller-service-repository flow, schema migrations, transactions, database constraints,
integration tests, OpenAPI docs, and Docker Compose. Pagination and better filtering would be the
first things I would add as the list endpoints grow.
