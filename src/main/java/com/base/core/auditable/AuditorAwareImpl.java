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
package com.base.core.auditable;


import com.base.config.security.service.SecurityContextImpl;
import com.base.core.authentication.user.model.User;
import com.base.core.authentication.user.repository.UserRepository;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author YISivlay
 */
@Component(value = "auditorProvider")
public class AuditorAwareImpl implements AuditorAware<User> {

    private final SecurityContextImpl securityContext;
    private final UserRepository userRepository;

    public AuditorAwareImpl(SecurityContextImpl securityContext, UserRepository userRepository) {
        this.securityContext = securityContext;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getCurrentAuditor() {
        var user = securityContext.authenticatedUser();
        return user != null ? Optional.of(user) : userRepository.findByUsername("system");
    }
}
