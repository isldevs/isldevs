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
package com.base.core.authentication.user.service;

import com.base.core.authentication.user.model.Authority;
import com.base.core.authentication.user.repository.UserRepository;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author YISivlay
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Autowired
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var user = userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		var roles = user.getRoles()
			.stream()
			.map(role -> role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName())
			.toArray(String[]::new);

		var authorities = user.getRoles()
			.stream()
			.flatMap(role -> role.getAuthorities().stream())
			.map(Authority::getAuthority)
			.toArray(String[]::new);

		var allAuthorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		Arrays.stream(authorities).map(SimpleGrantedAuthority::new).forEach(allAuthorities::add);

		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getUsername())
			.password(user.getPassword())
			.authorities(allAuthorities)
			.accountExpired(!user.isAccountNonExpired())
			.accountLocked(!user.isAccountNonLocked())
			.credentialsExpired(!user.isCredentialsNonExpired())
			.disabled(!user.isEnabled())
			.build();
	}

}
