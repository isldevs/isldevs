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
package com.base.core.schedule.service;

import com.base.config.security.keypairs.Keys;
import com.base.config.security.keypairs.RSAKeyPairRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;

/**
 * @author YISivlay
 */
@Component("rsaKeyRotator")
public class ScheduledKeyRotator implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledKeyRotator.class);

    private final RSAKeyPairRepository repository;
    private final Keys keys;

    @Autowired
    public ScheduledKeyRotator(RSAKeyPairRepository repository, Keys keys) {
        this.repository = repository;
        this.keys = keys;
    }

    @Override
    public void run() {
        try {
            var keyPairs = repository.findKeyPairs();
            var newestKey = keyPairs.stream()
                    .max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created));
            var shouldRotate = newestKey.isEmpty() ||
                    newestKey.get().created().toInstant().isBefore(Instant.now().minus(30, ChronoUnit.DAYS));

            if (shouldRotate) {
                String keyId = UUID.randomUUID().toString();
                Timestamp created = new Timestamp(System.currentTimeMillis());
                var newKey = keys.generateKeyPair(keyId, created);
                repository.save(newKey);
                logger.info("New rotated RSA key with ID {} on {}", keyId, created);
            } else {
                logger.info("RSA key not expired yet.");
            }
        } catch (Exception e) {
            logger.error("RSA key rotation failed", e);
        }
    }
}
