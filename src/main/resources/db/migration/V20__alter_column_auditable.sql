ALTER TABLE commune RENAME COLUMN created_date TO created_at;
ALTER TABLE commune RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE commune RENAME COLUMN last_modified_date TO updated_at;

ALTER TABLE config RENAME COLUMN created_date TO created_at;
ALTER TABLE config RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE config RENAME COLUMN last_modified_date TO updated_at;

ALTER TABLE district RENAME COLUMN created_date TO created_at;
ALTER TABLE district RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE district RENAME COLUMN last_modified_date TO updated_at;

ALTER TABLE file RENAME COLUMN created_date TO created_at;
ALTER TABLE file RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE file RENAME COLUMN last_modified_date TO updated_at;

ALTER TABLE office RENAME COLUMN created_date TO created_at;
ALTER TABLE office RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE office RENAME COLUMN last_modified_date TO updated_at;

ALTER TABLE province RENAME COLUMN created_date TO created_at;
ALTER TABLE province RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE province RENAME COLUMN last_modified_date TO updated_at;

ALTER TABLE village RENAME COLUMN created_date TO created_at;
ALTER TABLE village RENAME COLUMN last_modified_by TO updated_by;
ALTER TABLE village RENAME COLUMN last_modified_date TO updated_at;