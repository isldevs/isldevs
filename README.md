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
- Spring Boot 3.5.8, Spring Authorization Server 1.5.2
- PostgreSQL (dev) or MySQL
- Gradle (Wrapper included)
- Optional: GraalVM for native builds

## Installation
1. Clone the repo:
   ```bash
   git clone https://github.com/isldevs/isldevs
   git clone https://gitlab.com/isldevs/isldevs
   cd isldevs
   ```
2. Install dependencies:
   ```bash
   ./gradlew dependencies
   ```
3. Run:
   ```bash
   ./gradlew clean build
   ./gradlew bootRun -Pspring.profiles.active=dev
   ```

## Docker Setup
You can run iSLDevs using Docker and Docker Compose for a containerized deployment with PostgreSQL.

### Prerequisites
- Install [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/).
- Ensure the `Dockerfile` and `docker-compose.yml` files are in the project root.

### Dockerfile
The `Dockerfile` uses a multi-stage build:
- Builds the application with Java 25 JDK.
- Runs the standalone JAR with Java 25 JRE.
- Exposes port 8443 and sets up environment variables for PostgreSQL and Spring profiles.

### Docker Compose
The `docker-compose.yml` defines two services:
- `isldevs`: The iSLDevs Spring Boot application.
- `db`: A PostgreSQL 15 database (compatible with JDBC driver 42.7.5).

### Running with Docker Compose
1. Build and run the services:
   ```bash
   docker-compose up --build
   ```
2. Access API at base url `https://localhost:8443/api/v1`.
   ### Stopping the Containers
   To stop the services:
   ```bash
   docker-compose down
   ```
   To stop and remove volumes (including database data):
   ```bash
   docker-compose down -v
   ```
3. Restart Services:
   ###### Application
   ```bash
   sudo docker-compose restart isldevs
   ```
   ###### Database
   ```bash
   sudo docker-compose restart db
   ```
4. Log Services:
   ```bash
   sudo docker-compose logs isldevs
   ```

## JWT Key Management
JWT keys are encrypted with AES-256 and stored in a PostgreSQL `rsa_key_pairs` table, fetched dynamically for signing/verification. Encryption uses `JWT_PASSWORD` and `JWT_SALT` from the `config` table.

### Table Setup
```sql
CREATE TABLE rsa_key_pairs
(
   id          VARCHAR(36) PRIMARY KEY, -- UUID
   private_key TEXT NOT NULL,           -- AES-256 encrypted
   public_key  TEXT NOT NULL,           -- AES-256 encrypted
   created     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE config
(
   id           SERIAL PRIMARY KEY,
   name         VARCHAR(255) NOT NULL,
   key_name     VARCHAR(255) NOT NULL,
   value        TEXT         NOT NULL,
   active       BOOLEAN DEFAULT true,
   created_by   VARCHAR(255),
   created_date TIMESTAMP,
   updated_by   VARCHAR(255),
   updated_date TIMESTAMP
);
```
### Generate and Store Keys
Keys are generated programmatically (RSA 2048-bit) via the `Keys` class and stored in `rsa_key_pairs` on startup if none exist.

### Configure
Populate the `config` table with secure values:
```sql
INSERT INTO config (name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('JWT Password', 'JWT_PASSWORD', '$(openssl rand -base64 32)', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
       ('JWT Salt', 'JWT_SALT', '$(openssl rand -hex 16)', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
```

### Key Loading
The latest keypair (`id` as UUID) is fetched from `rsa_key_pairs` via JDBC, decrypted using `JWT_PASSWORD` and `JWT_SALT` from `config`, and used for JWT operations. The `id` is included as `kid` in JWT headers.

## SSL/TLS Setup
Generate a self-signed certificate for dev:
```bash
keytool -genkeypair -alias yourserver -keyalg RSA -keysize 4096 -validity 365 \
  -storetype PKCS12 -keystore src/main/resources/yourserver.p12 \
  -storepass changeit -keypass changeit -dname "CN=localhost" \
  -ext "SAN=DNS:localhost,IP:127.0.0.1"
```

## OAuth2 Testing with Postman
- **Authorization Code + PKCE**: GET `/oauth2/authorize`, POST `/oauth2/token` with `grant_type=authorization_code`
- **Client Credentials**: POST `/oauth2/token` with `grant_type=client_credentials`
- **JWT Bearer**: POST `/oauth2/token` with `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer`
- **Device Code**: POST `/oauth2/device_authorization`

[Full Postman configs](https://github.com/isldevs/isldevs/blob/master/docs/postman.md).

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

## License
Apache 2.0. See [LICENSE](LICENSE).
 
## About
iSLDevs: Secure OAuth2 with Java, Spring, and JDBC. 😎