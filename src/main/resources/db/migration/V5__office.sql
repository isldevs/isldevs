CREATE TABLE office
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id          BIGINT       REFERENCES office(id) ON DELETE SET NULL,
    hierarchy          VARCHAR(150),
    name_en            VARCHAR(100) NOT NULL UNIQUE,
    name_km            VARCHAR(150) UNIQUE,
    name_zh            VARCHAR(100) UNIQUE,
    created_by         VARCHAR(100),
    created_date       TIMESTAMP(0),
    last_modified_by   VARCHAR(100),
    last_modified_date TIMESTAMP(0)
);

CREATE INDEX idx_office_name_km ON office(name_km);
CREATE INDEX idx_office_name_zh ON office(name_zh);
