CREATE TABLE district
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    province_id BIGINT       NOT NULL REFERENCES province (id),
    type        VARCHAR(50)  NOT NULL,
    postal_code VARCHAR(50)  NOT NULL,
    name_en     VARCHAR(100) NOT NULL,
    name_km     VARCHAR(100) NOT NULL,
    name_zh     VARCHAR(100) DEFAULT NULL,
    created_by  VARCHAR(100) DEFAULT 'system',
    created_at  TIMESTAMP(0) DEFAULT now(),
    updated_by  VARCHAR(100) DEFAULT 'system',
    updated_at  TIMESTAMP(0) DEFAULT now()
);

CREATE UNIQUE INDEX idx_district_postal_code_key ON district (province_id, postal_code);
CREATE INDEX idx_district_postal_code_key_trgm ON district USING gin (postal_code gin_trgm_ops);
CREATE INDEX idx_district_name_en_key_trgm ON district USING gin (name_en gin_trgm_ops);
CREATE INDEX idx_district_name_km_key_trgm ON district USING gin (name_km gin_trgm_ops);
CREATE INDEX idx_district_name_zh_key_trgm ON district USING gin (name_zh gin_trgm_ops);

INSERT INTO authorities(authority) VALUES ('CREATE_DISTRICT');
INSERT INTO authorities(authority) VALUES ('UPDATE_DISTRICT');
INSERT INTO authorities(authority) VALUES ('DELETE_DISTRICT');
INSERT INTO authorities(authority) VALUES ('READ_DISTRICT');
