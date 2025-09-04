CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER DATABASE isldevs_db
    SET pg_trgm.similarity_threshold = 0.2;