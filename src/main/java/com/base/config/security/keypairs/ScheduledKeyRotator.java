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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;

/**
 * @author YISivlay
 */
@Component
public class ScheduledKeyRotator {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledKeyRotator.class);

    private final RSAKeyPairRepository repository;
    private final Keys keys;

    @Autowired
    public ScheduledKeyRotator(RSAKeyPairRepository repository, Keys keys) {
        this.repository = repository;
        this.keys = keys;
    }

    @Scheduled(cron = "0 0 0 */30 * *")
    public void autoRotateKey() {
        var keyPairs = repository.findKeyPairs();
        var newestKey = keyPairs.stream().max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created));
        var shouldRotate = newestKey.isEmpty() || newestKey.get().created().toInstant().isBefore(Instant.now().minus(30, ChronoUnit.DAYS));

        if (shouldRotate) {
            String keyId = UUID.randomUUID().toString();
            var created = new Timestamp(System.currentTimeMillis());
            var newKey = keys.generateKeyPair(keyId, created);
            repository.save(newKey);
            logger.info("ScheduledKeyRotator: Rotated RSA key with ID {} on new created {}", keyId, created);
        } else {
            logger.info("ScheduledKeyRotator: Key rotation not required yet.");
        }
    }

}
