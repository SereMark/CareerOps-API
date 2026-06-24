CREATE TABLE job_postings (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    source_url VARCHAR(2048),
    location VARCHAR(200),
    work_mode VARCHAR(20) NOT NULL,
    notes VARCHAR(5000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_job_postings_company
        FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT chk_job_postings_work_mode
        CHECK (work_mode IN ('ONSITE', 'HYBRID', 'REMOTE'))
);

CREATE INDEX idx_job_postings_company_title
    ON job_postings (company_id, title);

CREATE TABLE job_applications (
    id UUID PRIMARY KEY,
    job_posting_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(5000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_job_applications_posting UNIQUE (job_posting_id),
    CONSTRAINT fk_job_applications_posting
        FOREIGN KEY (job_posting_id) REFERENCES job_postings (id),
    CONSTRAINT chk_job_applications_status
        CHECK (status IN (
            'SAVED',
            'APPLIED',
            'SCREENING',
            'INTERVIEW',
            'OFFER',
            'ACCEPTED',
            'REJECTED',
            'WITHDRAWN'
        ))
);

CREATE INDEX idx_job_applications_status_updated
    ON job_applications (status, updated_at DESC);

CREATE TABLE application_status_events (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    note VARCHAR(2000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_application_status_events_target
        UNIQUE (application_id, new_status),
    CONSTRAINT fk_application_status_events_application
        FOREIGN KEY (application_id) REFERENCES job_applications (id),
    CONSTRAINT chk_application_status_events_previous_status
        CHECK (previous_status IS NULL OR previous_status <> new_status),
    CONSTRAINT chk_application_status_events_new_status
        CHECK (new_status IN (
            'SAVED',
            'APPLIED',
            'SCREENING',
            'INTERVIEW',
            'OFFER',
            'ACCEPTED',
            'REJECTED',
            'WITHDRAWN'
        )),
    CONSTRAINT chk_application_status_events_old_status
        CHECK (
            previous_status IS NULL OR previous_status IN (
                'SAVED',
                'APPLIED',
                'SCREENING',
                'INTERVIEW',
                'OFFER',
                'ACCEPTED',
                'REJECTED',
                'WITHDRAWN'
            )
        )
);

CREATE INDEX idx_application_status_events_timeline
    ON application_status_events (application_id, occurred_at, id);
