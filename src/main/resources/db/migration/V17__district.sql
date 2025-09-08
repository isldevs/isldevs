CREATE TABLE district
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    province_id        BIGINT NOT NULL REFERENCES province(id),
    type               VARCHAR(50) NOT NULL,
    name               VARCHAR(100) NOT NULL,
    postal_code        VARCHAR(50) NOT NULL,
    created_by         VARCHAR(100),
    created_date       TIMESTAMP(0),
    last_modified_by   VARCHAR(100),
    last_modified_date TIMESTAMP(0)
);

CREATE UNIQUE INDEX idx_district_postal_code_key ON district (province_id, postal_code);
CREATE INDEX idx_district_postal_code_key_trgm ON district USING gin(postal_code gin_trgm_ops);
CREATE INDEX idx_district_name_key_trgm ON district USING gin(name gin_trgm_ops);

INSERT INTO authorities(authority) VALUES ('CREATE_DISTRICT');
INSERT INTO authorities(authority) VALUES ('UPDATE_DISTRICT');
INSERT INTO authorities(authority) VALUES ('DELETE_DISTRICT');
INSERT INTO authorities(authority) VALUES ('READ_DISTRICT');
