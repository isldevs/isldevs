CREATE TABLE scheduled_job
(
    job_name        TEXT PRIMARY KEY,
    cron_expression TEXT NOT NULL,
    bean_name       TEXT NOT NULL,
    enabled         BOOLEAN DEFAULT true
);

CREATE TABLE scheduled_job_history
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_name         TEXT         NOT NULL,
    executed_at      TIMESTAMP(0) NOT NULL,
    next_executed_at TIMESTAMP(0),
    status           VARCHAR(20)  NOT NULL,
    error_message    TEXT
);
CREATE INDEX idx_sjh_job_name ON scheduled_job_history (job_name);

INSERT INTO scheduled_job (job_name, cron_expression, bean_name, enabled)
VALUES ('rotate_rsa_key', '0 0 0 */30 * *', 'rsaKeyRotator', true);

