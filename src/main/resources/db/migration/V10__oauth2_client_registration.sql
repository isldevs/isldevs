CREATE TABLE oauth2_client_registration
(
    registration_id          VARCHAR(100) PRIMARY KEY,
    client_id                VARCHAR(255)  NOT NULL,
    client_secret            VARCHAR(255)  NOT NULL,
    client_auth_method       VARCHAR(50)   NOT NULL,
    authorization_grant_type VARCHAR(50)   NOT NULL,
    redirect_uri             VARCHAR(2000) NOT NULL,
    scope                    VARCHAR(1000),
    authorization_uri        VARCHAR(2000),
    token_uri                VARCHAR(2000),
    user_info_uri            VARCHAR(2000),
    user_name_attribute      VARCHAR(100),
    client_name              VARCHAR(200)
);