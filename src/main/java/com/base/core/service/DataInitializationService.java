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
package com.base.core.service;

import com.base.core.authentication.role.repository.RoleRepository;
import com.base.core.authentication.user.model.Authority;
import com.base.core.authentication.role.model.Role;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.repository.AuthorityRepository;
import com.base.core.authentication.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

/**
 * @author YISivlay
 */
@Service
public class DataInitializationService {

    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializationService(AuthorityRepository authorityRepository,
                                     RoleRepository roleRepository,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        Authority fullAccess = authorityRepository.findByAuthority("FULL_ACCESS")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("FULL_ACCESS").build()));

        Authority readUser = authorityRepository.findByAuthority("READ_USER")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("READ_USER").build()));

        Authority createUser = authorityRepository.findByAuthority("CREATE_USER")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("CREATE_USER").build()));

        Authority updateUser = authorityRepository.findByAuthority("UPDATE_USER")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("UPDATE_USER").build()));

        Authority deleteUser = authorityRepository.findByAuthority("DELETE_USER")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("DELETE_USER").build()));

        Authority readRole = authorityRepository.findByAuthority("READ_ROLE")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("READ_ROLE").build()));

        Authority createRole = authorityRepository.findByAuthority("CREATE_ROLE")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("CREATE_ROLE").build()));

        Authority updateRole = authorityRepository.findByAuthority("UPDATE_ROLE")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("UPDATE_ROLE").build()));

        Authority deleteRole = authorityRepository.findByAuthority("DELETE_ROLE")
                .orElseGet(() -> authorityRepository.save(Authority.builder().authority("DELETE_ROLE").build()));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").authorities(Set.of(fullAccess)).build()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_USER")
                        .authorities(Set.of(readUser,createUser,updateUser,deleteUser,readRole,createRole,updateRole,deleteRole))
                        .build()
                ));

        userRepository.findByUsername("admin").orElseGet(() -> {
            User admin = User.builder()
                    .name("Admin")
                    .username("admin")
                    .email("admin@email.com")
                    .password(passwordEncoder.encode("admin@2025!"))
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            return userRepository.save(admin);
        });

        userRepository.findByUsername("system").orElseGet(() -> {
            User user = User.builder()
                    .name("System")
                    .username("system")
                    .email("system@email.com")
                    .password(passwordEncoder.encode("system@2025!"))
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            return userRepository.save(user);
        });

        userRepository.findByUsername("user").orElseGet(() -> {
            User user = User.builder()
                    .name("User")
                    .username("user")
                    .email("user@email.com")
                    .password(passwordEncoder.encode("user@2025!"))
                    .roles(Set.of(userRole))
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            return userRepository.save(user);
        });
    }
}