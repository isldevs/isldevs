CREATE TABLE request_log
(
    id           SERIAL PRIMARY KEY,
    entity_id    BIGINT       NOT NULL,
    entity       VARCHAR(150) NOT NULL,
    action       VARCHAR(150) NOT NULL,
    href         VARCHAR(255) NOT NULL,
    json         TEXT         NOT NULL,
    created_by   BIGINT       NOT NULL,
    created_date TIMESTAMP(0) NOT NULL
);