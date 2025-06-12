CREATE TABLE rsa_key_pairs
(
    id          VARCHAR(1000) NOT NULL PRIMARY KEY,
    private_key TEXT          NOT NULL,
    public_key  TEXT          NOT NULL,
    created     TIMESTAMP          NOT NULL,
    UNIQUE (id, created)
);