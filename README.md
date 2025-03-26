# Tomcat SSL Configuration Guide

## Overview
This document explains how to configure SSL/TLS for the embedded Tomcat server in the iSLDevs application using Spring Boot 3.4.3.

## Prerequisites
- Java 17+
- Spring Boot 3.2.4
- Tomcat 10.1.x (embedded with Spring Boot 3.2.4)
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

# PostgreSQL Configuration

This project demonstrates how to configure PostgreSQL in Spring Boot 3.2.4 using environment variables instead of property files, following security best practices.

## Features

- 🔒 100% environment variable driven configuration
- 🚀 Production-ready HikariCP connection pooling
- ⚡ No hardcoded credentials in property files
- 🌍 Works across all environments (local, Docker, Kubernetes, cloud)

## Prerequisites

- Java 17+
- Spring Boot 3.2.4
- PostgreSQL 12+
- Maven/Gradle

## Configuration

### Required Environment Variables

| Variable        | Example Value                         | Description                |
|-----------------|---------------------------------------|----------------------------|
| `DB_URL`        | `jdbc:postgresql://localhost:5432/db` | PostgreSQL JDBC URL        |
| `DB_USERNAME`   | `postgres`                            | Database username          |
| `DB_PASSWORD`   | `your_secure_password`                | Database password          |

### Optional Configuration

| Variable               | Default   | Description                          |
|------------------------|-----------|--------------------------------------|
| `DB_POOL_MAX_SIZE`     | 10        | Maximum connection pool size         |
| `DB_POOL_MIN_IDLE`     | 5         | Minimum idle connections             |
| `DB_CONNECTION_TIMEOUT`| 30000 (ms)| Connection timeout                   |
| `HIBERNATE_DDL_AUTO`   | validate  | Database schema initialization mode  |
| `HIBERNATE_SHOW_SQL`   | false     | Show SQL queries in logs             |

## Setup Guide

### Local Development

1. Set environment variables:
   ```bash
   # Linux/macOS
   export DB_URL=jdbc:postgresql://localhost:5432/mydb
   export DB_USERNAME=postgres
   export DB_PASSWORD=secret
   export DB_DRIVER_CLASS=org.postgresql.Driver

   # Windows
   set DB_URL=jdbc:postgresql://localhost:5432/mydb
   set DB_USERNAME=postgres
   set DB_PASSWORD=secret
   set DB_DRIVER_CLASS=org.postgresql.Driver

## IDE Configuration (IntelliJ/Eclipse)

### 🖥️ IntelliJ IDEA Setup
1. **Open Run Configurations**
   - Click the dropdown near "Run" → **Edit Configurations...**

2. **Add Environment Variables**
   - Select your Spring Boot application configuration
   - Under **Environment Variables**, add:
     ```
     DB_URL=jdbc:postgresql://localhost:5432/yourdb
     DB_USERNAME=youruser
     DB_PASSWORD=yourpass
     DB_DRIVER_CLASS=org.postgresql.Driver
     ```
   - Click **Apply** → **OK**

3. **Verify Variables**
   - Run the app and check logs for:
     ```
     PostgreSQL connection established to: jdbc:postgresql://...
     ```

### 🌑 Eclipse Setup
1. **Open Run Configurations**
   - Right-click project → **Run As** → **Run Configurations...**

2. **Add Environment Variables**
   - Select your Spring Boot application
     - Go to **Environment** tab → **Add**:

   | Name              | Value                                 |
   |-------------------|---------------------------------------|
   | `DB_URL`          | `jdbc:postgresql://localhost:5432/db` |
   | `DB_USERNAME`     | `youruser`                            |
   | `DB_PASSWORD`     | `yourpass`                            | 
   | `DB_DRIVER_CLASS` | `org.postgresql.Driver`               | 

   - Click **Apply** → **Run**