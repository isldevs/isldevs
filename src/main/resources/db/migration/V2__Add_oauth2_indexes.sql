-- Add indexes for better OAuth2 performance
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