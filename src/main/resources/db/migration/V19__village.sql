CREATE TABLE village
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    commune_id         BIGINT       NOT NULL REFERENCES commune (id),
    name               VARCHAR(100) NOT NULL,
    postal_code        VARCHAR(50)  NOT NULL,
    created_by         VARCHAR(100),
    created_date       TIMESTAMP(0),
    last_modified_by   VARCHAR(100),
    last_modified_date TIMESTAMP(0)
);

CREATE UNIQUE INDEX idx_village_postal_code_key ON village (commune_id, postal_code);
CREATE INDEX idx_village_postal_code_key_trgm ON village USING gin (postal_code gin_trgm_ops);
CREATE INDEX idx_village_name_key_trgm ON village USING gin (name gin_trgm_ops);

INSERT INTO authorities(authority)
VALUES ('CREATE_VILLAGE');
INSERT INTO authorities(authority)
VALUES ('UPDATE_VILLAGE');
INSERT INTO authorities(authority)
VALUES ('DELETE_VILLAGE');
INSERT INTO authorities(authority)
VALUES ('READ_VILLAGE');
