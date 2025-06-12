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
package com.base.config.security.service.keypairs;


import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author YISivlay
 */
@Component
public class RSAKeyPairRepositoryJWKSource implements JWKSource<SecurityContext>, OAuth2TokenCustomizer<JwtEncodingContext> {

    private final RSAKeyPairRepository rsaKeyPairRepository;

    @Autowired
    public RSAKeyPairRepositoryJWKSource(RSAKeyPairRepository rsaKeyPairRepository) {
        this.rsaKeyPairRepository = rsaKeyPairRepository;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext) throws KeySourceException {
        return rsaKeyPairRepository.findKeyPairs().stream()
                .max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created))
                .map(keyPair -> {
                    RSAKey rsaKey = new RSAKey.Builder(keyPair.publicKey())
                            .privateKey(keyPair.privateKey())
                            .keyID(keyPair.id())
                            .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                            .build();
                    return jwkSelector.select(new com.nimbusds.jose.jwk.JWKSet(rsaKey));
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public void customize(JwtEncodingContext context) {
        rsaKeyPairRepository.findKeyPairs().stream()
                .max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created))
                .ifPresent(keyPair -> context.getJwsHeader().keyId(keyPair.id()));
    }
}
