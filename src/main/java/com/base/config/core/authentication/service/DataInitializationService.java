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
        var adminUser = adminUserList.isEmpty() ? null : adminUserList.getFirst();
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