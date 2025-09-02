ALTER TABLE users
    ADD COLUMN provider_id         VARCHAR(100) NULL,
    ADD COLUMN provider            VARCHAR(100) NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_avatar_url VARCHAR(255) NULL;