CREATE TABLE province
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type               VARCHAR(50) NOT NULL,
    name               VARCHAR(100) NOT NULL,
    postal_code        VARCHAR(50) NOT NULL,
    created_by         VARCHAR(100),
    created_date       TIMESTAMP(0),
    last_modified_by   VARCHAR(100),
    last_modified_date TIMESTAMP(0)
);

CREATE UNIQUE INDEX idx_province_postal_code_key ON province (postal_code);
CREATE INDEX idx_province_postal_code_key_trgm ON province USING gin(postal_code gin_trgm_ops);
CREATE INDEX idx_province_name_key_trgm ON province USING gin(name gin_trgm_ops);

INSERT INTO authorities(authority) VALUES ('CREATE_PROVINCE');
INSERT INTO authorities(authority) VALUES ('UPDATE_PROVINCE');
INSERT INTO authorities(authority) VALUES ('DELETE_PROVINCE');
INSERT INTO authorities(authority) VALUES ('READ_PROVINCE');
