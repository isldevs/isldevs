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
package com.base.config.security.keypairs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author YISivlay
 */
@Component
public class RSAKeyPairRowMapper implements RowMapper<RSAKeyPairRepository.RSAKeyPair> {

    private final RSAPrivateKeyConverter rsaPrivateKeyConverter;
    private final RSAPublicKeyConverter rsaPublicKeyConverter;

    @Autowired
    public RSAKeyPairRowMapper(RSAPrivateKeyConverter rsaPrivateKeyConverter,
                               RSAPublicKeyConverter rsaPublicKeyConverter) {
        this.rsaPrivateKeyConverter = rsaPrivateKeyConverter;
        this.rsaPublicKeyConverter = rsaPublicKeyConverter;
    }

    @Override
    public RSAKeyPairRepository.RSAKeyPair mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            var id = rs.getString("id");
            var created = rs.getTimestamp("created");
            var privateKeyStr = rs.getString("private_key");
            var publicKeyStr = rs.getString("public_key");

            var privateKey = (RSAPrivateKey) rsaPrivateKeyConverter.convertFromString(privateKeyStr);
            var publicKey = (RSAPublicKey) rsaPublicKeyConverter.convertFromString(publicKeyStr);

            return new RSAKeyPairRepository.RSAKeyPair(id, created, publicKey, privateKey);
        } catch (IOException e) {
            throw new RuntimeException("Failed to map RSA key pair", e);
        }
    }
}
