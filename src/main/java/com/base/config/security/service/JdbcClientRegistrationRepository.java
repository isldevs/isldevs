/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.config.security.service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Service;

/**
 * @author YISivlay
 */
@Service
public class JdbcClientRegistrationRepository implements ClientRegistrationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper rowMapper;

    public JdbcClientRegistrationRepository(JdbcTemplate jdbcTemplate,
                                            RowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        List<ClientRegistration> list = jdbcTemplate.query("SELECT * FROM oauth2_client_registration WHERE registration_id = ?",
                                                           (rs,
                                                            rowMapper) -> ClientRegistration.withRegistrationId(rs.getString("registration_id"))
                                                                                            .clientId(rs.getString("client_id"))
                                                                                            .clientSecret(rs.getString("client_secret"))
                                                                                            .clientAuthenticationMethod(ClientAuthenticationMethod.valueOf(rs.getString("client_auth_method")))
                                                                                            .authorizationGrantType(new AuthorizationGrantType(rs.getString("authorization_grant_type")))
                                                                                            .redirectUri(rs.getString("redirect_uri"))
                                                                                            .scope(rs.getString("scope") != null ? rs.getString("scope")
                                                                                                                                     .split(",") : new String[]{})
                                                                                            .authorizationUri(rs.getString("authorization_uri"))
                                                                                            .tokenUri(rs.getString("token_uri"))
                                                                                            .userInfoUri(rs.getString("user_info_uri"))
                                                                                            .jwkSetUri(rs.getString("jwk_set_uri"))
                                                                                            .userNameAttributeName(rs.getString("user_name_attribute"))
                                                                                            .clientName(rs.getString("client_name"))
                                                                                            .build(),
                                                           registrationId);

        return list.isEmpty() ? null : list.get(0);
    }

    public void save(ClientRegistration client) {
        jdbcTemplate.update("INSERT INTO oauth2_client_registration " + "(registration_id, client_id, client_secret, client_auth_method, authorization_grant_type, " + "redirect_uri, scope, authorization_uri, token_uri, user_info_uri, user_name_attribute, client_name, jwk_set_uri) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            client.getRegistrationId(),
                            client.getClientId(),
                            client.getClientSecret(),
                            client.getClientAuthenticationMethod()
                                  .getValue(),
                            client.getAuthorizationGrantType()
                                  .getValue(),
                            client.getRedirectUri(),
                            String.join(",",
                                        client.getScopes()),
                            client.getProviderDetails()
                                  .getAuthorizationUri(),
                            client.getProviderDetails()
                                  .getTokenUri(),
                            client.getProviderDetails()
                                  .getUserInfoEndpoint()
                                  .getUri(),
                            client.getProviderDetails()
                                  .getUserInfoEndpoint()
                                  .getUserNameAttributeName(),
                            client.getClientName(),
                            client.getProviderDetails()
                                  .getJwkSetUri());
    }

}
