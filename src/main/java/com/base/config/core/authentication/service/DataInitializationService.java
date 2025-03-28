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
import java.util.List;

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
        List<Role> adminRoleList = entityManager.createQuery("SELECT r FROM Role r WHERE r.name = 'ROLE_ADMIN'", Role.class).getResultList();
        Role adminRole = adminRoleList.isEmpty() ? null : adminRoleList.getFirst();
        if (adminRole == null) {
            adminRole = Role.builder().name("ROLE_ADMIN").build();
            entityManager.persist(adminRole);
        }

        List<Authority> adminFullAccessList = entityManager.createQuery("SELECT a FROM Authority a WHERE a.authority = 'ADMIN_FULL_ACCESS'", Authority.class).getResultList();
        Authority adminFullAccess = adminFullAccessList.isEmpty() ? null : adminFullAccessList.getFirst();
        if (adminFullAccess == null) {
            adminFullAccess = Authority.builder().role(adminRole).authority("ADMIN_FULL_ACCESS").build();
            entityManager.persist(adminFullAccess);
        }

        List<User> adminUserList = entityManager.createQuery("SELECT u FROM User u WHERE u.username = 'admin'", User.class).getResultList();
        User adminUser = adminUserList.isEmpty() ? null : adminUserList.getFirst();
        if (adminUser == null) {
            String encodedPassword = passwordEncoder.encode("admin@2025!");
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