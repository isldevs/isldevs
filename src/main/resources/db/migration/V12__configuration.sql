CREATE TABLE config
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name               VARCHAR(150) NOT NULL,
    code               VARCHAR(150) NOT NULL UNIQUE,
    value              VARCHAR(255) NOT NULL,
    enabled            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by VARCHAR(100),
    created_at TIMESTAMP(0),
    updated_by VARCHAR(100),
    updated_at TIMESTAMP(0)
);

INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('JWT Password', 'JWT_PASSWORD', 'iamsldevs', 'system', now(), 'system', now());

INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('JWT Salt', 'JWT_SALT', '69736c64657673', 'system', now(), 'system', now());