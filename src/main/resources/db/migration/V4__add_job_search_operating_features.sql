ALTER TABLE job_postings
    ADD COLUMN target_lane VARCHAR(40) NOT NULL DEFAULT 'JAVA_BACKEND',
    ADD COLUMN seniority VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN salary_min_gross_huf INTEGER,
    ADD COLUMN salary_max_gross_huf INTEGER,
    ADD COLUMN role_fit_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN mentoring_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN salary_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN engineering_practices_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN learning_signal_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN hybrid_fit_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN hard_veto_reason VARCHAR(500),
    ADD CONSTRAINT chk_job_postings_target_lane
        CHECK (target_lane IN (
            'JAVA_BACKEND',
            'JAVA_HEAVY_SOFTWARE_ENGINEER',
            'GRADUATE_BACKEND',
            'OTHER'
        )),
    ADD CONSTRAINT chk_job_postings_seniority
        CHECK (seniority IN (
            'UNKNOWN',
            'INTERN',
            'JUNIOR',
            'ASSOCIATE',
            'MID',
            'SENIOR'
        )),
    ADD CONSTRAINT chk_job_postings_salary_min
        CHECK (salary_min_gross_huf IS NULL OR salary_min_gross_huf >= 0),
    ADD CONSTRAINT chk_job_postings_salary_max
        CHECK (salary_max_gross_huf IS NULL OR salary_max_gross_huf >= 0),
    ADD CONSTRAINT chk_job_postings_salary_range
        CHECK (
            salary_min_gross_huf IS NULL
            OR salary_max_gross_huf IS NULL
            OR salary_min_gross_huf <= salary_max_gross_huf
        ),
    ADD CONSTRAINT chk_job_postings_role_fit_score
        CHECK (role_fit_score BETWEEN 0 AND 30),
    ADD CONSTRAINT chk_job_postings_mentoring_score
        CHECK (mentoring_score BETWEEN 0 AND 25),
    ADD CONSTRAINT chk_job_postings_salary_score
        CHECK (salary_score BETWEEN 0 AND 20),
    ADD CONSTRAINT chk_job_postings_engineering_practices_score
        CHECK (engineering_practices_score BETWEEN 0 AND 10),
    ADD CONSTRAINT chk_job_postings_learning_signal_score
        CHECK (learning_signal_score BETWEEN 0 AND 10),
    ADD CONSTRAINT chk_job_postings_hybrid_fit_score
        CHECK (hybrid_fit_score BETWEEN 0 AND 5);

CREATE TABLE next_actions (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    due_date DATE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_next_actions_application
        FOREIGN KEY (application_id) REFERENCES job_applications (id),
    CONSTRAINT chk_next_actions_type
        CHECK (type IN (
            'TAILOR_CV',
            'APPLY',
            'FOLLOW_UP',
            'PREPARE_SCREENING',
            'PREPARE_TECHNICAL_INTERVIEW',
            'SEND_THANK_YOU',
            'NEGOTIATE',
            'OTHER'
        ))
);

CREATE INDEX idx_next_actions_due_open
    ON next_actions (completed_at, due_date);

CREATE INDEX idx_next_actions_application_due
    ON next_actions (application_id, due_date);
