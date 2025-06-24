/**
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
package com.base.config.core.authentication.service;

import com.base.config.core.authentication.model.Authority;
import com.base.config.core.authentication.model.Role;
import com.base.config.core.authentication.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

/**
 * @author YISivlay
 */
@Service
public class DataInitializationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataInitializationService.class);

    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DataInitializationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void dataInitialization() {
        var adminRoleList = entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'ADMIN'", Role.class).getResultList();
        var adminRole = adminRoleList.isEmpty() ? null : adminRoleList.getFirst();
        if (adminRole == null) {
            adminRole = Role.builder().name("ADMIN").build();
            entityManager.persist(adminRole);
        }

        var adminFullAccessList = entityManager.createQuery("SELECT a FROM Authority a WHERE a.authority = 'FULL_ACCESS'", Authority.class).getResultList();
        var adminFullAccess = adminFullAccessList.isEmpty() ? null : adminFullAccessList.getFirst();
        if (adminFullAccess == null) {
            adminFullAccess = Authority.builder().role(adminRole).authority("FULL_ACCESS").build();
            entityManager.persist(adminFullAccess);
        }

        var adminUserList = entityManager.createQuery("SELECT u FROM User u WHERE u.username = 'admin'", User.class).getResultList();
        var adminUser = adminUserList.isEmpty() ? User.builder().build() : adminUserList.getFirst();
        if (adminUser == null) {
            var encodedPassword = passwordEncoder.encode("admin@2025!");
            adminUser = User.builder()
                    .username("admin")
                    .password(encodedPassword)
                    .roles(new HashSet<>(Collections.singletonList(adminRole)))
                    .enabled(true)
                    .isAccountNonExpired(true)
                    .isCredentialsNonExpired(true)
                    .isAccountNonLocked(true)
                    .build();
            entityManager.persist(adminUser);
            LOGGER.info("Data initialization insert successfully");
        }
    }
}