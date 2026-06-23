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
