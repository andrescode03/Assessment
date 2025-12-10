CREATE TABLE affiliates (
    id BIGSERIAL PRIMARY KEY,
    document VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    salary DECIMAL(19, 2) NOT NULL,
    affiliation_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE credit_applications (
    id BIGSERIAL PRIMARY KEY,
    affiliate_id BIGINT NOT NULL,
    requested_amount DECIMAL(19, 2) NOT NULL,
    term_months INTEGER NOT NULL,
    proposed_rate DECIMAL(5, 4),
    application_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    risk_score INTEGER,
    risk_level VARCHAR(20),
    decision_reason VARCHAR(255),
    evaluation_date TIMESTAMP,
    CONSTRAINT fk_affiliate FOREIGN KEY (affiliate_id) REFERENCES affiliates(id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);
