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

import com.base.config.security.keypairs.RSAKeyPairRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author YISivlay
 */
@Service
public final class JwtAuthenticationServiceImpl implements JwtAuthenticationService {

    private static final long EXPIRATION = 1000 * 60 * 15;

    private final Clock clock;
    private final RSAKeyPairRepository rsaKeyPairRepository;

    @Autowired
    public JwtAuthenticationServiceImpl(final Clock clock,
                                        final RSAKeyPairRepository rsaKeyPairRepository) {
        this.clock = clock;
        this.rsaKeyPairRepository = rsaKeyPairRepository;
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, EXPIRATION);
    }

    @Override
    public String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        RSAPrivateKey privateKey = rsaKeyPairRepository.findKeyPairs().stream()
                .max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created))
                .map(RSAKeyPairRepository.RSAKeyPair::privateKey)
                .orElseThrow(() -> new IllegalStateException("No RSA key pair found in the repository"));

        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now(clock)))
                .expiration(Date.from(Instant.now(clock).plusSeconds(expiration)))
                .claim("authorities", authorities)
                .signWith(privateKey)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final var username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public Claims extractAllClaims(String token) {
        try {
            RSAPublicKey publicKey = rsaKeyPairRepository.findKeyPairs().stream()
                    .max(Comparator.comparing(RSAKeyPairRepository.RSAKeyPair::created))
                    .map(RSAKeyPairRepository.RSAKeyPair::publicKey)
                    .orElseThrow(() -> new IllegalStateException("No RSA key pair found in the repository"));

            return Jwts
                    .parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid or expired JWT token", e);
        }
    }
}
