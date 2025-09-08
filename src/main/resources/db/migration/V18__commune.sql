CREATE TABLE commune
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    district_id        BIGINT NOT NULL REFERENCES district(id),
    type               VARCHAR(50) NOT NULL,
    name               VARCHAR(100) NOT NULL,
    postal_code        VARCHAR(50) NOT NULL,
    created_by         VARCHAR(100),
    created_date       TIMESTAMP(0),
    last_modified_by   VARCHAR(100),
    last_modified_date TIMESTAMP(0)
);

CREATE UNIQUE INDEX idx_commune_postal_code_key ON commune (district_id, postal_code);
CREATE INDEX idx_commune_postal_code_key_trgm ON commune USING gin(postal_code gin_trgm_ops);
CREATE INDEX idx_commune_name_key_trgm ON commune USING gin(name gin_trgm_ops);

INSERT INTO authorities(authority) VALUES ('CREATE_COMMUNE');
INSERT INTO authorities(authority) VALUES ('UPDATE_COMMUNE');
INSERT INTO authorities(authority) VALUES ('DELETE_COMMUNE');
INSERT INTO authorities(authority) VALUES ('READ_COMMUNE');
