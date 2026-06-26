DROP INDEX idx_job_postings_company_title;

CREATE INDEX idx_job_postings_created
    ON job_postings (created_at DESC);

CREATE INDEX idx_job_postings_company_created
    ON job_postings (company_id, created_at DESC);

ALTER TABLE job_applications
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
