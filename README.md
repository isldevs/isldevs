# Tomcat SSL Configuration Guide

## Overview
This document explains how to configure SSL/TLS for the embedded Tomcat server in the iSLDevs application using Spring Boot 3.4.3.

## Prerequisites
- Java 17+
- Spring Boot 3.4.3
- Tomcat 10.1.x (embedded with Spring Boot 3.4.3)
- Valid SSL certificate (or self-signed for development)

## Certificate Generation
For development environments, generate a self-signed certificate:

```bash
keytool -genkeypair -alias yourserver -keyalg RSA -keysize 4096 \
  -validity 365 -storetype PKCS12 -keystore src/main/resources/yourserver.p12 \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost" -ext "SAN=DNS:localhost,IP:127.0.0.1"
```
Check certificate details
```bash
keytool -list -v -keystore src/main/resources/yourserver.p12 -storepass changeit
```

Check supported protocols (should only show TLS 1.2/1.3)
```bash
nmap --script ssl-enum-ciphers -p 8443 localhost
```

## Java Class Configuration
Java configuration class:
- src/main/java/com/base/config/TomcatSSLConfig.java

# H2 Database Configuration Guide

## Overview
This guide covers H2 in-memory database setup with Spring Boot, including console access, security configuration, and production considerations.

## Features
- In-memory database for rapid development
- Web-based management console
- SQL compatibility with minimal configuration
- Embedded and server modes

## Quick Start
### 1. Add Dependencies
```
<!-- Gradle -->
runtimeOnly 'com.h2database:h2'
```
### 2. Check properties.yml
```
spring:
  datasource:
    url: jdbc:h2:mem:isldevs
    username: sa
    password: password
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
```
### 3. Security allow /h2-console
- src/main/java/com/base/config/SecurityConfig.java
### 4. Access /h2-console on web
- http://localhost:8080/api/h2-console