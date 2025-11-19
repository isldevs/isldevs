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
package com.base.config.security;

import com.base.config.GlobalConfig;
import com.base.config.security.keypairs.Keys;
import com.base.config.security.keypairs.RSAKeyPairService;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.*;

/**
 * @author YISivlay
 */
@Configuration
public class KeyConfig {

    private final Logger logger = LoggerFactory.getLogger(KeyConfig.class);
    private final GlobalConfig config;

    @Autowired
    public KeyConfig(final GlobalConfig config) {
        this.config = config;
    }

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(config.getJwtPassword(), config.getJwtSalt());
    }

    @Bean
    public NimbusJwtEncoder jwtEncoder(JWKSource<SecurityContext> customJWKSource) {
        return new NimbusJwtEncoder(customJWKSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JwtEncoder encoder,
                                                            OAuth2TokenCustomizer<JwtEncodingContext> customTokenCustomizer) {
        var generator = new JwtGenerator(encoder);
        generator.setJwtCustomizer(customTokenCustomizer);
        return new DelegatingOAuth2TokenGenerator(generator, new OAuth2AccessTokenGenerator(), new OAuth2RefreshTokenGenerator());
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> rsaKeyRotationAutomatic(RSAKeyPairService repository) {
        return _ -> {
            var existingKey = repository.findKeyPairs()
                    .stream()
                    .max(Comparator.comparing(RSAKeyPairService.RSAKeyPair::created));

            if (existingKey.isEmpty()) {
                var keys = new Keys();
                var keyPair = keys.generateKeyPair(UUID.randomUUID()
                        .toString(), new Timestamp(System.currentTimeMillis()));
                repository.save(keyPair);
                logger.info("Initial RSA Key created at startup");
            }
        };
    }

}
