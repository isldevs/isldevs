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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author YISivlay
 */
@Component
public class JdbcRSAKeyPairRepository implements RSAKeyPairRepository {

    private final JdbcTemplate jdbc;
    private final RSAPublicKeyConverter rsaPublicKeyConverter;
    private final RSAPrivateKeyConverter rsaPrivateKeyConverter;
    private final RowMapper<RSAKeyPair> keyPairRowMapper;

    @Autowired
    public JdbcRSAKeyPairRepository(final JdbcTemplate jdbc,
                                    final RSAPublicKeyConverter rsaPublicKeyConverter,
                                    final RSAPrivateKeyConverter rsaPrivateKeyConverter,
                                    final RowMapper<RSAKeyPair> keyPairRowMapper) {
        this.jdbc = jdbc;
        this.rsaPublicKeyConverter = rsaPublicKeyConverter;
        this.rsaPrivateKeyConverter = rsaPrivateKeyConverter;
        this.keyPairRowMapper = keyPairRowMapper;
    }

    @Override
    public List<RSAKeyPair> findKeyPairs() {
        return this.jdbc.query("SELECT * FROM rsa_key_pairs ORDER BY created DESC", this.keyPairRowMapper);
    }

    @Override
    public void save(RSAKeyPair keyPair) {
        var sql = """
                  INSERT INTO rsa_key_pairs (id, private_key, public_key, created) VALUES (?, ?, ?, ?)
                  ON CONFLICT ON CONSTRAINT rsa_key_pairs_id_created_key DO NOTHING
                  """;
        try (var privateBAOS = new ByteArrayOutputStream(); var publicBAOS = new ByteArrayOutputStream()) {
            this.rsaPrivateKeyConverter.serialize(keyPair.privateKey(), privateBAOS);
            this.rsaPublicKeyConverter.serialize(keyPair.publicKey(), publicBAOS);
            var updated = this.jdbc.update(sql, keyPair.id(), privateBAOS.toString(StandardCharsets.UTF_8), publicBAOS
                    .toString(StandardCharsets.UTF_8), new Date(keyPair.created()
                            .getTime()));
            Assert.state(updated == 0 || updated == 1, "no more than one record should have been updated");
        } catch (IOException e) {
            throw new IllegalArgumentException("there's been an exception", e);
        }
    }

}
