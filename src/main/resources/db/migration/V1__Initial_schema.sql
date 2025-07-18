CREATE TABLE oauth2_authorization
(
    id                            varchar(100) NOT NULL,
    registered_client_id          varchar(100) NOT NULL,
    principal_name                varchar(200) NOT NULL,
    authorization_grant_type      varchar(100) NOT NULL,
    authorized_scopes             varchar(1000) DEFAULT NULL,
    attributes                    text          DEFAULT NULL,
    state                         varchar(500)  DEFAULT NULL,
    authorization_code_value      text          DEFAULT NULL,
    authorization_code_issued_at  timestamp     DEFAULT NULL,
    authorization_code_expires_at timestamp     DEFAULT NULL,
    authorization_code_metadata   text          DEFAULT NULL,
    access_token_value            text          DEFAULT NULL,
    access_token_issued_at        timestamp     DEFAULT NULL,
    access_token_expires_at       timestamp     DEFAULT NULL,
    access_token_metadata         text          DEFAULT NULL,
    access_token_type             varchar(100)  DEFAULT NULL,
    access_token_scopes           varchar(1000) DEFAULT NULL,
    oidc_id_token_value           text          DEFAULT NULL,
    oidc_id_token_issued_at       timestamp     DEFAULT NULL,
    oidc_id_token_expires_at      timestamp     DEFAULT NULL,
    oidc_id_token_metadata        text          DEFAULT NULL,
    refresh_token_value           text          DEFAULT NULL,
    refresh_token_issued_at       timestamp     DEFAULT NULL,
    refresh_token_expires_at      timestamp     DEFAULT NULL,
    refresh_token_metadata        text          DEFAULT NULL,
    user_code_value               text          DEFAULT NULL,
    user_code_issued_at           timestamp     DEFAULT NULL,
    user_code_expires_at          timestamp     DEFAULT NULL,
    user_code_metadata            text          DEFAULT NULL,
    device_code_value             text          DEFAULT NULL,
    device_code_issued_at         timestamp     DEFAULT NULL,
    device_code_expires_at        timestamp     DEFAULT NULL,
    device_code_metadata          text          DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent
(
    registered_client_id varchar(100)  NOT NULL,
    principal_name       varchar(200)  NOT NULL,
    authorities          varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE oauth2_registered_client
(
    id                            varchar(100)                            NOT NULL,
    client_id                     varchar(100)                            NOT NULL,
    client_id_issued_at           timestamp     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret                 varchar(200)  DEFAULT NULL,
    client_secret_expires_at      timestamp     DEFAULT NULL,
    client_name                   varchar(200)                            NOT NULL,
    client_authentication_methods varchar(1000)                           NOT NULL,
    authorization_grant_types     varchar(1000)                           NOT NULL,
    redirect_uris                 varchar(1000) DEFAULT NULL,
    post_logout_redirect_uris     varchar(1000) DEFAULT NULL,
    scopes                        varchar(1000)                           NOT NULL,
    client_settings               varchar(2000)                           NOT NULL,
    token_settings                varchar(2000)                           NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username                   VARCHAR(50)  NOT NULL UNIQUE,
    password                   VARCHAR(500) NOT NULL,
    name                       VARCHAR(255),
    email                      VARCHAR(255),
    provider                   VARCHAR(50)  NOT NULL DEFAULT 'LOCAL',
    provider_id                VARCHAR(100),
    avatar_url                 TEXT,
    locale                     VARCHAR(20),
    access_token               TEXT,
    refresh_token              TEXT,
    token_expiry               TIMESTAMP,
    enabled                    BOOLEAN      NOT NULL DEFAULT true,
    is_account_non_expired     BOOLEAN      NOT NULL DEFAULT true,
    is_account_non_locked      boolean      NOT NULL DEFAULT true,
    is_credentials_non_expired boolean      NOT NULL DEFAULT true
);

CREATE TABLE authorities
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    authority VARCHAR(50) NOT NULL
);

CREATE INDEX idx_oauth2_authorization_token_values ON oauth2_authorization (
                                                                            authorization_code_value,
                                                                            access_token_value,
                                                                            refresh_token_value
    );

CREATE INDEX idx_oauth2_authorization_expiry ON oauth2_authorization (
                                                                      authorization_code_expires_at,
                                                                      access_token_expires_at,
                                                                      refresh_token_expires_at
    );