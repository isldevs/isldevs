CREATE TABLE logs
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entity_id  BIGINT       NOT NULL,
    entity     VARCHAR(150) NOT NULL,
    action     VARCHAR(150) NOT NULL,
    href       VARCHAR(255) NOT NULL,
    json       TEXT DEFAULT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(0) NOT NULL
);