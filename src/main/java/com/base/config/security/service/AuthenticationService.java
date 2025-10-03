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
package com.base.config.security.service;

import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author YISivlay
 */
public interface AuthenticationService {

	String extractUsername(String token);

	<T> T extractClaim(String token, Function<Claims, T> claimsResolver);

	String generateToken(UserDetails userDetails);

	String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

	String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration);

	boolean isTokenValid(String token, UserDetails userDetails);

	boolean isTokenExpired(String token);

	Date extractExpiration(String token);

	Claims extractAllClaims(String token);

}
