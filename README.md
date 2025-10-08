# iSLDevs

iSLDevs is a secure, scalable OAuth2 Authorization Server built with Java 25 and Spring Boot 3.5.6. It supports multiple grant types, social logins (Google, GitHub, Facebook), and JWTs with RSA cryptography, all backed by PostgreSQL and Flyway migrations.

## Why iSLDevs?

Simplifies secure backend development with:

- OAuth2 flows: Authorization Code (PKCE), Client Credentials, JWT Bearer, Device Code, Refresh Token
- Social login integration with user mapping
- Encrypted JWT keys stored in PostgreSQL
- SSL/TLS via embedded Tomcat
- Postman-ready endpoints for testing

## Requirements

- Java 25 (Gradle toolchain)
- Spring Boot 3.5.6, Spring Authorization Server 1.5.2
- PostgreSQL (dev) or MySQL
- Gradle (Wrapper included)
- Optional: GraalVM for native builds

## Installation

1. Clone the repo:

   ```bash
   git clone https://github.com/isldevs/isldevs
   cd isldevs
   ```

2. Install dependencies:

   ```bash
   ./gradlew dependencies
   ```

3. Set environment variables:

   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/isldevs_db
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=your-secure-password
   export JWT_PRIVATE_KEY_PATH=/secure/path/key.private
   export JWT_PERSISTENCE_PASSWORD=$(openssl rand -base64 32)
   export JWT_PERSISTENCE_SALT=$(openssl rand -hex 16)
   ```

4. Run:

   ```bash
   ./gradlew clean build
   ./gradlew bootRun -Pspring.profiles.active=dev
   ```

## JWT Key Management

JWT keys are stored in a PostgreSQL table (`jwt_keys`) and encrypted with AES-256.

### Table Setup

```sql
CREATE TABLE rsa_key_pairs (
    id SERIAL PRIMARY KEY,
    private_key TEXT NOT NULL, -- Encrypted
    public_key TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Generate Keys

```bash
openssl genrsa -out key.private 2048
openssl rsa -in key.private -pubout -out key.public
```

### Configure

In `application.properties`:

```properties
jwt.key.id=isldevs
jwt.key.public=classpath:key.public
jwt.key.private=${JWT_PRIVATE_KEY_PATH}
jwt.persistence.password=${JWT_PERSISTENCE_PASSWORD}
jwt.persistence.salt=${JWT_PERSISTENCE_SALT}
```

## SSL/TLS Setup

Generate a self-signed certificate for dev:

```bash
keytool -genkeypair -alias yourserver -keyalg RSA -keysize 4096 -validity 365 \
  -storetype PKCS12 -keystore src/main/resources/yourserver.p12 \
  -storepass changeit -keypass changeit -dname "CN=localhost" \
  -ext "SAN=DNS:localhost,IP:127.0.0.1"
```

## OAuth2 Testing with Postman

- **Authorization Code + PKCE**:
    - GET `https://localhost:8443/oauth2/authorize?response_type=code&client_id={client-id}&...`
    - POST `https://localhost:8443/oauth2/token` with `grant_type=authorization_code`
- **Client Credentials**:
    - POST `https://localhost:8443/oauth2/token` with `grant_type=client_credentials`
- **JWT Bearer**:
    - POST `https://localhost:8443/oauth2/token` with `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer`
- **Device Code**:
    - POST `https://localhost:8443/oauth2/device_authorization`

See full Postman configs for details.

## Project Structure

```
/isldevs
├── build.gradle
├── src/main/java/com/base/ISLDevsApplication.java
├── src/main/resources/application.properties
├── src/main/resources/application-dev.properties
├── src/main/resources/application-prod.properties
├── src/main/resources/templates/ (Thymeleaf)
├── src/main/resources/db/migration/ (Flyway)
└── LICENSE (Apache 2.0)
```

## Gradle Tasks

- Clean: `./gradlew clean`
- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Test: `./gradlew test`
- Format: `./gradlew spotlessApply`

## Environment Variables

- `SPRING_PROFILES_ACTIVE=dev|prod`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_PRIVATE_KEY_PATH`, `JWT_PERSISTENCE_PASSWORD`, `JWT_PERSISTENCE_SALT`

## License

- [ ] Apache 2.0. See LICENSE.