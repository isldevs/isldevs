CREATE INDEX idx_office_name_en_trgm ON office USING gin (name_en gin_trgm_ops);
CREATE INDEX idx_office_name_km_trgm ON office USING gin (name_km gin_trgm_ops);
CREATE INDEX idx_office_name_zh_trgm ON office USING gin (name_zh gin_trgm_ops);

CREATE INDEX idx_user_name_trgm ON users USING gin (name gin_trgm_ops);
CREATE INDEX idx_user_username_trgm ON users USING gin (username gin_trgm_ops);
CREATE INDEX idx_user_email_trgm ON users USING gin (email gin_trgm_ops);

CREATE INDEX idx_role_name_trgm ON roles USING gin (name gin_trgm_ops);

CREATE INDEX idx_file_entity_trgm ON file USING gin (entity gin_trgm_ops);
CREATE INDEX idx_file_name_trgm ON file USING gin (name gin_trgm_ops);

CREATE INDEX idx_config_name_trgm ON config USING gin (name gin_trgm_ops);
CREATE INDEX idx_config_code_trgm ON config USING gin (code gin_trgm_ops);
CREATE INDEX idx_config_value_trgm ON config USING gin (value gin_trgm_ops);

CREATE INDEX idx_authorities_authority_trgm ON authorities USING gin (authority gin_trgm_ops);
