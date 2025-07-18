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
package com.base.config.security.data;


import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * @author YISivlay
 */
public class ClientAuthenticationToken extends AbstractAuthenticationToken {

    private final String clientId;
    private final String clientAssertion;

    public ClientAuthenticationToken(String clientId, String clientAssertion) {
        super(null);
        this.clientId = clientId;
        this.clientAssertion = clientAssertion;
        setAuthenticated(false);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientAssertion() {
        return clientAssertion;
    }

    @Override
    public Object getCredentials() {
        return clientAssertion;
    }

    @Override
    public Object getPrincipal() {
        return clientId;
    }
}
