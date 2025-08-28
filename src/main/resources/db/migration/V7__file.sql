CREATE TABLE file
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entity             VARCHAR(150) NOT NULL,
    entity_id          BIGINT       NOT NULL,
    name               VARCHAR(100) NOT NULL,
    size               INT,
    mime_type          VARCHAR(100) NOT NULL,
    location           VARCHAR(200) NOT NULL,
    storage_type       SMALLINT     NOT NULL,
    created_by         VARCHAR(100),
    created_date       TIMESTAMP(0),
    last_modified_by   VARCHAR(100),
    last_modified_date TIMESTAMP(0)
);

CREATE UNIQUE INDEX file_entity_entity_id_key ON file (entity, entity_id);
