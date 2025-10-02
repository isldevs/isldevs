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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component("rsaKeyRotator")
public class ScheduledKeyRotator implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(ScheduledKeyRotator.class);

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
      ensureAtLeastOneKeyExists();
      performScheduledRotation();
    } catch (Exception e) {
      logger.error("RSA key rotation failed", e);
    }
  }

  private void ensureAtLeastOneKeyExists() {
    if (repository.findKeyPairs().isEmpty()) {
      generateEmergencyKey();
    }
  }

  private void performScheduledRotation() {
    var existingKey =
        repository.findKeyPairs().stream()
            .max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created));
    var shouldRotate =
        existingKey.isEmpty()
            || existingKey
                .get()
                .created()
                .toInstant()
                .isBefore(Instant.now().minus(30, ChronoUnit.DAYS));

    if (shouldRotate) {
      var keyId = UUID.randomUUID().toString();
      var created = new Timestamp(System.currentTimeMillis());
      var newKey = keys.generateKeyPair(keyId, created);
      repository.save(newKey);
      logger.info("New rotated RSA key with ID {} on {}", keyId, created);
    }
  }

  private void generateEmergencyKey() {
    var keyId = UUID.randomUUID().toString();
    var created = new Timestamp(System.currentTimeMillis());
    var newKey = keys.generateKeyPair(keyId, created);
    repository.save(newKey);
    logger.warn("Generated new RSA key due to missing keys");
  }
}
