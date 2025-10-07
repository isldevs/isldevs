# iSLDevs
iSLDevs is an all-in-one developer toolset focused on building secure, 
scalable applications with advanced authentication, user, and location management features. 
It provides a flexible OAuth2 Authorization Server supporting multiple grant types and social login integrations, backed by JWT and RSA cryptography for robust security. 
Its extensive configuration options streamline build automation, security, scheduling, caching, and internationalization, 
enabling developers to accelerate development while maintaining high standards of security and maintainability.

## Why iSLDevs?
This project aims to simplify complex security and data management tasks for developers. 
The core features include:

### 🚀 Features
- OAuth2 Authorization Server (Spring Authorization Server 1.5.x)
- Supported flows:
    - Authorization Code with PKCE
    - Client Credentials
    - JWT Bearer Grant
    - Refresh Token
- Configurable SSL/TLS support (Tomcat HTTPS)
- Database-backed client, user, role, and permission management
- Token customization (JWT with claims)
- Postman-ready endpoints for testing

### 🌐 OAuth2 Client (Social Login)
- Login with GitHub, Google, Facebook, and centralized user principal mapping (convert external providers to internal user model)
- Automatic token exchange and persistence with Spring Security

## ⚙️ Requirements
- Java 25 (toolchain configured in Gradle)
- Spring Boot 3.5.6
- Spring Authorization Server 1.5.x
- PostgreSQL/MySQL (via JDBC; dev profile uses PostgreSQL)
- Tomcat 10.1.39 (embedded; version forced via Gradle resolution strategy)
- Gradle (Wrapper included)
- Optional: GraalVM for native image builds

## ⚙️ Installing
Build isldevs from the source and install dependencies:
1. **Clone the repository:**
> git clone https://github.com/isldevs/isldevs

2. **Navigate to the project directory:**
> cd isldevs

3. **Install the dependencies:**
```
./gradlew dependencies --refresh-dependencies
```
4. **Setup datasource connection**:

    This project demonstrates how to configure PostgreSQL
    in Spring Boot 3.5.x using environment variables instead of property files,
    following security best practices.

- Required Environment Variables

| Variable          | Example Value                          | Description       |
|-------------------|----------------------------------------|-------------------|
| `DB_URL`          | `jdbc:{youurl}://localhost:{port}}/db` | JDBC URL          |
| `DB_USERNAME`     | `youruser`                             | Username          |
| `DB_PASSWORD`     | `yourpass`                             | Password          |
| `DB_DRIVER_CLASS` | `yourdriverclassname`                  | Driver class name |

- Optional Configuration

| Variable                       | Default        | Description                                               |
|--------------------------------|----------------|-----------------------------------------------------------|
| `DB_POOL_MAX_SIZE`             | 10             | Maximum connection pool size                              |
| `DB_POOL_MIN_IDLE`             | 5              | Minimum idle connections                                  |
| `DB_CONNECTION_TIMEOUT`        | 5000 (ms)      | Fail fast in production (5s)                              |
| `DB_IDLE_TIMEOUT `             | 120000 (ms)    | Reclaim unused connections faster (2m)                    |
| `DB_MAX_LIFETIME `             | 1800000 (ms)   | Prevent stale connections (30m)                           |
| `DB_LEAK_DETECTION_THRESHOLD ` | 60000 (ms)     | Lead to resource exhaustion and application crashes (60s) |

5. Run project
```
./gradlew clean build
./gradlew bootRun -Pspring.profiles.active=dev
OR
./gradlew clean build
./gradlew bootRun -Pspring.profiles.active=prod
```

## Tomcat SSL Config Guide
### Overview
This document explains 
how to configure SSL/TLS for the embedded Tomcat server 
in the iSLDevs application using Spring Boot 3.5.x.

### Certificate Generation
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

## Setup Guide
### Local Development

1. Set environment variables:
   ```bash
   # Linux/macOS
   export DB_URL=jdbc:{youurl}://localhost:{port}}/db
   export DB_USERNAME=youruser
   export DB_PASSWORD=yourpass
   export DB_DRIVER_CLASS=yourdriverclassname

   # Windows
   set DB_URL=jdbc:{youurl}://localhost:{port}}/db
   set DB_USERNAME=youruser
   set DB_PASSWORD=yourpass
   set DB_DRIVER_CLASS=yourdriverclassname

## IDE Config (IntelliJ/Eclipse)
### 🖥️ IntelliJ IDEA Setup
1. **Open Run Configurations**
   - Click the dropdown near "Run" → **Edit Configurations...**

2. **Add Environment Variables**
   - Select your Spring Boot application configuration
   - Under **Environment Variables**, add:
     ```
     DB_URL=jdbc:{youurl}://localhost:{port}}/db
     DB_USERNAME=youruser
     DB_PASSWORD=yourpass
     DB_DRIVER_CLASS=yourdriverclassname
     ```
   - Click **Apply** → **OK**

### 🌑 Eclipse Setup
1. **Open Run Configurations**
   - Right-click project → **Run As** → **Run Configurations...**

2. **Add Environment Variables**
   - Select your Spring Boot application
      - Go to **Environment** tab → **Add**:

   | Name              | Value                                  |
   |-------------------|----------------------------------------|
   | `DB_URL`          | `jdbc:{youurl}://localhost:{port}}/db` |
   | `DB_USERNAME`     | `youruser`                             |
   | `DB_PASSWORD`     | `yourpass`                             | 
   | `DB_DRIVER_CLASS` | `yourdriverclassname`                  | 

   - Click **Apply** → **Run**

## Environment Configuration
### Set Spring Profile

application.properties
```bash
spring.profiles.active=dev
spring.env.file=classpath:config/.dev

#spring.profiles.active=prod
#spring.env.file:classpath:config/.prod
```

### Development
application-dev.properties
```
DB_URL=jdbc:{youurl}://localhost:{port}}/db
DB_USERNAME=youruser
DB_PASSWORD=yourpass
DB_DRIVER_CLASS=yourdriverclassname

DB_POOL_MAX_SIZE=15
DB_POOL_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000
DB_IDLE_TIMEOUT=600000
DB_MAX_LIFETIME=1800000
DB_LEAK_DETECTION_THRESHOLD=0

HIBERNATE_DIALECT=yourhibernatedialect
HIBERNATE_DDL_AUTO=update
HIBERNATE_SHOW_SQL=true
HIBERNATE_FORMAT_SQL=true

REDIS_ENABLED=false
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=password
```

### Production
application-prod.properties
```
DB_URL=jdbc:{youurl}://localhost:{port}}/db
DB_USERNAME=youruser
DB_PASSWORD=yourpass
DB_DRIVER_CLASS=yourdriverclassname

DB_POOL_MAX_SIZE=25
DB_POOL_MIN_IDLE=10
DB_CONNECTION_TIMEOUT=5000
DB_IDLE_TIMEOUT=120000
DB_MAX_LIFETIME=900000
DB_LEAK_DETECTION_THRESHOLD=60000

HIBERNATE_DIALECT=yourhibernatedialect
HIBERNATE_DDL_AUTO=validate
HIBERNATE_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=true

REDIS_ENABLED=false
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=password
```
## Spring Boot OAuth 2.1 Authorization Server Testing with Postman (Client ID Flows)
This provides specific Postman endpoint configurations for testing each client ID flow.

### Prerequisites
* **Postman:** Download and install Postman from [www.postman.com](https://www.postman.com/).
* **Running Application:** Ensure your Spring Boot application is running and accessible.
* **Environment Variables (Optional):** Consider setting up Postman environment variables for host, port, and client secrets.

### 🔑 Client Flows & Postman Endpoints
Here are the Postman endpoints for each client ID flow:

#### 1. Web/Mobile App Client (Authorization Code + PKCE)
* **Client ID:** `{client-id}`
* **Grant Type:** Authorization Code with PKCE
   * **Authorization Endpoint:**
      * **Method:** `GET`
      * **URL:** `https://{host}:{port}/oauth2/authorize`
      * **Query Parameters:**
         * `response_type`: `code`
         * `client_id`: `{client-id}`
         * `redirect_uri`: `https://{redirect-uri}:{port}/login/oauth2/code/{client-id}`
         * `scope`: `openid profile email`
         * `code_challenge`: `{code_challenge}`
         * `code_challenge_method`: `S256`
         * `state`: `{random_state}`

   * **Token Endpoint (Authorization Code):**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/token`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
         * `Authorization`: Basic base64(`{client-id}`:`{client-secret}`)
      * **Body (x-www-form-urlencoded):**
         * `grant_type`: `authorization_code`
         * `code`: `{authorization_code}` (Obtained from the Authorization Endpoint redirect)
         * `redirect_uri`: `https://{redirect-uri}:{port}/login/oauth2/code/{client-id}`
         * `code_verifier`: `{code_verifier}`

   * **Token Endpoint (Refresh Token):**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/token`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
         * `Authorization`: Basic base64(`{client-id}`:`{client-secret}`)
      * **Body (x-www-form-urlencoded):**
         * `grant_type`: `refresh_token`
         * `refresh_token`: `{refresh_token}`

   * **PKCE Code Generation (JavaScript Example):**
      1.  **Generate Code Verifier and Code Challenge:**
          * Create a random string. It should be between 43 and 128 characters long.
          * Example (JavaScript):
             ```javascript
             function generateCodeVerifier() {
                 const array = new Uint32Array(56);
                 window.crypto.getRandomValues(array);
                 return Array.from(array, dec => ('0' + dec.toString(16)).slice(-2)).join('');
             }
             const codeVerifier = generateCodeVerifier();
             console.log("Code Verifier:", codeVerifier);
            
             async function generateCodeChallenge(codeVerifier) {
                 const data = new TextEncoder().encode(codeVerifier);
                 const digest = await crypto.subtle.digest("SHA-256", data);
                 return btoa(String.fromCharCode(...new Uint8Array(digest)))
                        .replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
             }
             generateCodeChallenge(codeVerifier).then(challenge => {
                 console.log("Code Challenge:", challenge);
             });
             ```
      2.  **Use in Authorization Request:**
          * Send the `code_challenge` and `code_challenge_method=S256` in the authorization endpoint request.
      3.  **Use Code verifier in Token Request:**
          * Send the original `code_verifier` in the token endpoint request.

#### 2. Machine-to-Machine Client (Client Credentials)
* **Client ID:** `service-account`
* **Grant Type:** Client Credentials
   * **Token Endpoint:**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/token`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
         * `Authorization`: Basic base64(`{client-id}`:`{client-secret}`)
      * **Body (x-www-form-urlencoded):**
         * `grant_type`: `client_credentials`
         * `scope`: `api.internal monitoring.read`

#### 3. Microservice Client (JWT Bearer)
* **Client ID:** `microservice`
* **Grant Type:** JWT Bearer
   * **Token Endpoint:**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/token`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
      * **Body (x-www-form-urlencoded):**
         * `grant_type`: `urn:ietf:params:oauth:grant-type:jwt-bearer`
         * `assertion`: `{signed_jwt}`
         * `client_id`: `{client-id}`

   * **JWT Creation Notes:**
      1.  **Obtain Private Key:**
          * Retrieve the private key associated with the `microservice` client. This key is used to sign the JWT.
      2.  **Construct JWT Claims:**
          * Create a JSON object with the required JWT claims.
          * Required claims:
         * `iss` (Issuer): The issuer of the JWT (your authorization server).
         * `sub` (Subject): The `client_id` (`microservice`).
         * `aud` (Audience): The token endpoint URL.
         * `exp` (Expiration Time): The expiration time of the JWT (in seconds since epoch).
         * `iat` (Issued At): The time the JWT was issued (in seconds since epoch).
           * Example (JSON):
             ```json
             {
                 "iss": "https://{host}:{port}",
                 "sub": "microservice",
                 "aud": "https://{host}:{port}/oauth2/token",
                 "exp": 1678886400,
                 "iat": 1678882800
             }
             ```
      3.  **Sign the JWT:**
          * Sign the JWT using the private key and the appropriate algorithm (e.g., RS256).
          * Libraries in most programming languages can handle this.
      4.  **Use in Token Request:**
          * Send the signed JWT as the `assertion` parameter in the token endpoint request.
#### 4. Device Client (TV/IoT)
* **Client ID:** `iot-device`
* **Grant Type:** Device Code
   * **Device Authorization Endpoint:**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/device_authorization`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
      * **Body (x-www-form-urlencoded):**
         * `client_id`: `{client-id}`
         * `scope`: `openid device.manage`

   * **Token Endpoint (Polling):**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/token`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
         * `Authorization`: Basic base64(`{client-id}`:`{client-secret}`)
      * **Body (x-www-form-urlencoded):**
         * `grant_type`: `urn:ietf:params:oauth:grant-type:device_code`
         * `device_code`: `{device_code}` (Obtained from the Device Authorization Endpoint)

   * **Token Endpoint (Refresh Token):**
      * **Method:** `POST`
      * **URL:** `https://{host}:{port}/oauth2/token`
      * **Headers:**
         * `Content-Type`: `application/x-www-form-urlencoded`
         * `Authorization`: Basic base64(`{client-id}`:`{client-secret}`)
      * **Body (x-www-form-urlencoded):**
         * `grant_type`: `refresh_token`
         * `refresh_token`: `{refresh_token}` (Obtained from the initial token response)
#### 5. Login via GitHub, Google
```
Noted:
Client secret and id handle value in table, go to config value of CLIENT_ID and CLIENT_SECRET in table config. Then restart your service.
```

## Important Notes

* Replace `{host}` and `{port}` with your actual host and port values.
* The `secret` client secrets are for development purposes only. Use secure storage in production.
* For the JWT Bearer flow, you'll need to implement JWT creation in your application.
* For the Device Code flow, be aware of the user interaction required.
* For all flows, ensure your client secrets are handled securely.
* Remember to replace any bracketed placeholders with actual values.
* Ensure that your redirect URIs are correct for your frontend application.
* Adjust the scopes to match the access requirements of your clients.
* Be mindful of token expiration times and refresh token usage.

### Adding Apache 2.0 License Headers
This project uses the `gradle-license-plugin` by Hierynomus to automatically add the Apache 2.0 license header to all Java source files. This ensures compliance with the Apache 2.0 license and clearly defines the terms under which the software is distributed.

**Steps to Add the License Header:**
1.  **Add the Plugin Dependency to `build.gradle`:**
    Open your `build.gradle` file and add the following line to the `plugins` block:

    ```gradle
    plugins {
        // ... other plugins
        id "com.github.hierynomus.license" version "0.16.1"
    }
    ```
    Make sure to check the plugin's GitHub repository ([https://github.com/hierynomus/gradle-license](https://github.com/hierynomus/gradle-license)) for the latest version if needed.

2.  **Create the `LICENSE` File:**
    In the root of your project directory, create a new file named `LICENSE`.

   3.  **Populate `LICENSE` with the Apache 2.0 License Text:**
       Paste the following content into the `LICENSE` file, making sure to replace `[yyyy]` with the appropriate year and `[name of copyright owner]` with the correct copyright holder:

       ```
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
       ```

4.  **Configure the License Plugin in `build.gradle`:**
    Add the following `license` block to your `build.gradle` file:

    ```gradle
    license {
        header = rootProject.file('LICENSE')
        // You can add exclusions if needed:
        // exclude "**/*.properties"
        // exclude "**/*.yml"
        // exclude "**/*.md"
    }
    ```

5.  **Apply the License Headers:**
    Open your terminal in the root of your project directory and run the following Gradle command:

    ```bash
    ./gradlew spotlessApply
    ```

    This will add the license header to all your Java source files that are missing it or have an incorrect one.

6.  **Verify License Headers:**
    You can verify that all files have the correct license header by running:

    ```bash
    ./gradlew spotlessCheck
    ```

    This task will fail if any files are missing the header or have an incorrect one.

**Important Considerations:**
* **Backup:** Before running `spotlessApply` for the first time, it's recommended to commit your current changes or create a backup of your project.
* **Existing Headers:** The plugin might replace existing license headers with the configured one. Review the changes carefully after running `spotlessApply`.
* **Exclusions:** Use the `exclude` configuration option to prevent the plugin from adding headers to specific files or directories (e.g., configuration files, documentation).
* **Copyright Year:** Ensure the copyright year in your `LICENSE` is accurate.
* **CI/CD Integration:** Consider adding the `gradle spotlessCheck` task to your GitHub Actions workflow (in `gradle.yml`) to automatically verify license headers during your CI builds. This helps maintain consistency.

## **Project Dependency Management**
### **Overview**
This project uses the Gradle Versions Plugin to maintain dependency hygiene, security, and stability. 
The system provides automated checks for outdated libraries with configurable update policies.

### **Quick Start**
#### **Check for Dependency Updates**
1.Standard check
```bash
./gradlew dependencyUpdate
```

2.Stable releases only
```bash
./gradlew dependencyUpdate -Drevision=release
```

## JWT Key Management and Configuration Guide
This document explains how to manage and configure RSA key pairs for JWT signing and verification, 
including secure storage and key loading in a Spring Boot application.

---

### 1. Generate RSA Key Pair
Use OpenSSL to generate a 2048-bit RSA private key and extract its public key:

```bash
# Generate private key
openssl genrsa -out key.private 2048

# Extract public key
openssl rsa -in key.private -pubout -out key.public
```

### 2. Store Keys
Place the generated keys (key.private and key.public) in the src/main/resources directory for easy classpath loading.

### 3. Convert Salt Value
The salt value is used for encrypting the private key or other cryptographic operations. You can derive the salt from a meaningful string by converting it to hexadecimal.

For example, for "isldevs":
```bash
echo -n "isldevs" | xxd -p
# Output: 69736c64657673
# Use the hex string 69736c64657673 as your salt value.
```

## 🔒 Dependency Locking
This project uses Gradle dependency locking to ensure consistent builds across all environments.
A gradle.lockfile is automatically generated and tracked in version control.
This file captures the exact resolved versions of all dependencies and prevents unexpected upgrades due to dynamic or transitive versions.

Dependency locking is enabled via dependencyLocking in build.gradle.
* 🔧 To update the lock file:
  Use the following command to regenerate it after modifying dependencies:

```
./gradlew dependencies --write-locks
```
* To refresh all dependencies and update the lock file:

```
./gradlew dependencies --write-locks --refresh-dependencies
```

* To verify all dependencies:

```
./gradlew dependencyUpdates
```
💡 The gradle.lockfile is intentionally located at the project root, following Gradle's standard behavior.


---

## 🧰 Stack & Tooling (detected)
- Language: Java 25
- Build: Gradle (Wrapper included)
- Frameworks:
  - Spring Boot 3.5.6
  - Spring Web, Spring MVC (embedded Tomcat 10.1.39)
  - Spring Security + Spring Authorization Server 1.5.x
  - Spring Data JPA (Hibernate)
  - Thymeleaf (templates configured under classpath:templates)
  - Optional: Spring Data Redis (disabled by default)
  - Flyway (database migrations)
- Database: PostgreSQL by default in dev profile (JDBC). MySQL possible with driver swap.
- Native image: GraalVM Native Build Tools plugin
- Packaging: Bootable JAR and WAR (war plugin enabled)

## ▶️ Entry Points
- Main class: `com.base.ISLDevsApplication`
- Spring profile: default `dev` (see `src/main/resources/application.properties`)
- Web server: Embedded Tomcat (forced to 10.1.39 in Gradle)

## 🚀 How to Run
- Build and run with the dev profile:
  - ./gradlew clean build
  - ./gradlew bootRun -Pspring.profiles.active=dev
- Switch to prod profile:
  - ./gradlew bootRun -Pspring.profiles.active=prod
- Run as JAR after build:
  - java -Dspring.profiles.active=dev -jar build/libs/isldevs-0.0.1-SNAPSHOT.jar
- Run as WAR (if deploying to external container):
  - Produce WAR via: ./gradlew clean build
  - Deploy generated WAR to a Servlet 6.0+ compatible container (Tomcat 10.1+)

## 🔧 Useful Gradle Tasks (scripts)
- Application
  - ./gradlew bootRun — run the app
  - ./gradlew build — compile and package
  - ./gradlew test — run unit tests
- Code quality
  - ./gradlew spotlessApply — format code
  - ./gradlew dependencyUpdates — check dependency versions
- Database (Flyway)
  - ./gradlew flywayInfo
  - ./gradlew flywayMigrate
  - ./gradlew flywayClean  # caution: destructive
- Native Image (GraalVM)
  - ./gradlew nativeCompile  # produces native binary (requires GraalVM toolchain)

## 🔐 Environment Variables
You can configure the application via Spring properties files or environment variables.

- Profiles
  - SPRING_PROFILES_ACTIVE=dev|prod (default is dev in application.properties)

- Database (Spring’s standard variables)
  - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/isldevs_db
  - SPRING_DATASOURCE_USERNAME=postgres
  - SPRING_DATASOURCE_PASSWORD=secret
  - SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

- HikariCP tuning (optional)
  - SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=15
  - SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
  - SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000
  - SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=600000
  - SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=1800000

- TODO: Social login client IDs/secrets for GitHub/Google/Facebook should be documented with exact property names once configured.
- NOTE: A custom `DB_*` variable approach is described earlier in this README; consider unifying on Spring’s standard env names. TODO: consolidate configuration docs.

## 🧪 Tests
- Run all tests: ./gradlew test
- Run with debug logs: ./gradlew test --info
- Run a single test class (example): ./gradlew test --tests "com.base.SomeTestClass"

## 🗂️ Project Structure (abridged)
```
/ (project root)
├─ build.gradle
├─ settings.gradle
├─ gradlew / gradlew.bat
├─ gradle/
├─ src/
│  ├─ main/
│  │  ├─ java/com/base/
│  │  │  ├─ ISLDevsApplication.java
│  │  │  ├─ config/**  (security, global config, etc.)
│  │  │  └─ ...
│  │  └─ resources/
│  │     ├─ application.properties
│  │     ├─ application-dev.properties
│  │     ├─ application-pord.properties
│  │     ├─ templates/  (Thymeleaf)
│  │     └─ db/ (Flyway)
│  └─ test/
│     └─ java/ (tests)
├─ README.md
└─ LICENSE
```

## 📄 License
- Source files include Apache License 2.0 headers.
- TODO: Add a top-level LICENSE file (Apache-2.0) to match headers and clarify licensing.
