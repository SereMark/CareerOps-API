# Five-minute demo

This demo runs through the main CareerOps workflow: create a company, score a job posting, create an
application, move it through two statuses, add a follow-up task, schedule a screen, and check the
dashboard.

The data below is fictional. The point is to show the backend behavior quickly, especially the
workflow rules, validation, transactions, persistence, OpenAPI docs, and tests behind the API.

## Start the project

```bash
docker compose up -d --wait
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Open [Swagger UI](http://localhost:8080/swagger-ui.html). You can complete the rest of the demo
there without installing another API client.

## 1. Create a company

Run `POST /api/companies`:

```json
{
  "name": "Example Engineering",
  "websiteUrl": "https://example.com",
  "notes": "Product company in Budapest"
}
```

Copy the `id` from the response.

## 2. Add and score a job posting

Run `POST /api/job-postings` and replace `companyId`:

```json
{
  "companyId": "paste-company-id-here",
  "title": "Junior Java Backend Developer",
  "sourceUrl": "https://example.com/careers/java-backend",
  "location": "Budapest",
  "workMode": "HYBRID",
  "targetLane": "JAVA_BACKEND",
  "seniority": "JUNIOR",
  "salaryMinGrossHuf": 910000,
  "salaryMaxGrossHuf": 1120000,
  "roleFitScore": 28,
  "mentoringScore": 20,
  "salaryScore": 18,
  "engineeringPracticesScore": 8,
  "learningSignalScore": 8,
  "hybridFitScore": 5,
  "notes": "Spring Boot and PostgreSQL"
}
```

The response should include `triageScore: 87` and `triagePriority: "PRIORITIZE"`. Copy the new
posting `id`.

## 3. Create an application

Run `POST /api/job-applications`:

```json
{
  "jobPostingId": "paste-posting-id-here",
  "notes": "CV tailored and ready"
}
```

The response should show `SAVED` as the current status. Copy the application `id`.

## 4. Record two status changes

Run `POST /api/job-applications/{id}/status`:

```json
{
  "targetStatus": "APPLIED",
  "note": "Submitted through the company careers page"
}
```

Run the same endpoint once more:

```json
{
  "targetStatus": "SCREENING",
  "note": "Recruiter call booked for Friday"
}
```

## 5. Add a next action

Run `POST /api/next-actions`:

```json
{
  "applicationId": "paste-application-id-here",
  "type": "PREPARE_SCREENING",
  "dueDate": "2026-07-03",
  "notes": "Review the job ad, CV, and company notes"
}
```

This creates a follow-up item instead of leaving the next step only in a note.

## 6. Schedule an interview round

Run `POST /api/interview-rounds`:

```json
{
  "applicationId": "paste-application-id-here",
  "roundType": "HR_SCREEN",
  "scheduledAt": "2026-07-04T09:00:00Z",
  "format": "VIDEO",
  "contactName": "Recruiter",
  "prepNotes": "Prepare intro, salary range, and why Java/Spring"
}
```

The default outcome is `SCHEDULED`.

## 7. Check the history and dashboard

Run `GET /api/job-applications/{id}/history`. The response should contain:

1. the initial `SAVED` event;
2. the move from `SAVED` to `APPLIED`;
3. the move from `APPLIED` to `SCREENING`.

To check the workflow validation, try moving directly from `SCREENING` to `OFFER`. The API should
return `409 Conflict` with the error code `INVALID_STATUS_TRANSITION`.

Run `GET /api/dashboard`. The response should show one application in `SCREENING`, one open next
action, and the scheduled interview if it falls within the next seven days.

## Optional offer step

If the process reaches an offer, run `POST /api/offers`:

```json
{
  "applicationId": "paste-application-id-here",
  "grossMonthlyHuf": 980000,
  "benefits": "Cafeteria and standard equipment",
  "hybridPolicy": "Budapest hybrid",
  "reviewPromise": "Salary review after probation",
  "decision": "PENDING",
  "decisionNotes": "Compare salary, mentoring, and engineering practices before deciding"
}
```

This keeps the offer decision tied to the application record instead of storing it in a separate
note.
