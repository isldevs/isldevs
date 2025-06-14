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


import com.base.config.security.keypairs.KeyProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

/**
 * @author YISivlay
 */
@Service
public class JwtAssertionGeneratorService {

    private final KeyProvider keyProvider;

    @Autowired
    public JwtAssertionGeneratorService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String generateClientAssertion() throws JOSEException {
        var now = Instant.now();
        var claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://localhost:8443/api/v1")
                .subject("microservice")
                .audience("https://localhost:8443/api/v1/oauth2/token")
                .jwtID("fc76c58c-ffdc-48f8-ab4c-ede9d113487f")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();

        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        var signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new RSASSASigner(keyProvider.privateKey()));

        return signedJWT.serialize();
    }
}
