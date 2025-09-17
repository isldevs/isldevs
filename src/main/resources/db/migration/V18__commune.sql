CREATE TABLE commune
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    province_id BIGINT       NOT NULL REFERENCES province (id),
    district_id BIGINT       NOT NULL REFERENCES district (id),
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

CREATE UNIQUE INDEX idx_commune_postal_code_key ON commune (district_id, postal_code);
CREATE INDEX idx_commune_postal_code_key_trgm ON commune USING gin (postal_code gin_trgm_ops);
CREATE INDEX idx_commune_name_en_key_trgm ON commune USING gin (name_en gin_trgm_ops);
CREATE INDEX idx_commune_name_km_key_trgm ON commune USING gin (name_km gin_trgm_ops);
CREATE INDEX idx_commune_name_zh_key_trgm ON commune USING gin (name_zh gin_trgm_ops);

INSERT INTO authorities(authority) VALUES ('CREATE_COMMUNE');
INSERT INTO authorities(authority) VALUES ('UPDATE_COMMUNE');
INSERT INTO authorities(authority) VALUES ('DELETE_COMMUNE');
INSERT INTO authorities(authority) VALUES ('READ_COMMUNE');
