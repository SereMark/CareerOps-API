CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    website_url VARCHAR(2048),
    notes VARCHAR(5000),
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_companies_normalized_name UNIQUE (normalized_name)
);

CREATE INDEX idx_companies_active_name
    ON companies (archived, normalized_name);
