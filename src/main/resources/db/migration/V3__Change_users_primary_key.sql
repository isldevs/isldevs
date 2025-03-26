-- V3__Change_users_primary_key.sql
BEGIN;

-- Step 1: First drop the foreign key constraint from authorities
ALTER TABLE authorities DROP CONSTRAINT IF EXISTS fk_authorities_users;

-- Step 2: Add the new id column
ALTER TABLE users ADD COLUMN id BIGSERIAL;

-- Step 3: Drop the existing primary key constraint on username
ALTER TABLE users DROP CONSTRAINT users_pkey;

-- Step 4: Set id as the new primary key
ALTER TABLE users ADD PRIMARY KEY (id);

-- Step 5: Ensure username remains unique
ALTER TABLE users ADD CONSTRAINT uk_username UNIQUE (username);

-- Step 6: Recreate the foreign key constraint
ALTER TABLE authorities ADD CONSTRAINT fk_authorities_users
    FOREIGN KEY (username) REFERENCES users (username);

-- Step 7: Recreate the composite index
CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);

COMMIT;