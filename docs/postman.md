
# iSLDevs API – Postman Collection Guide

This document describes how to use the **iSLDevs Collections** Postman collection as a guideline for all developers who clone this repository.

> Import the collection into Postman and set your `URL` environment variable (e.g. `https://localhost:8443/api/v1`).

---

## Global Notes

- All protected endpoints require `Authorization: Bearer <access_token>`.
- Use the **OAuth2** folders (especially `Web/Mobile Apps`, `Device Client (TV / IoT)`, `Machine-to-Machine Client (Client Credentials)`, and `Microservice Client (JWT Bearer)`) to obtain tokens.
- Collection-level global variables:
  - `tenant-header` / `tenant-header-value` are set for multi-tenant headers.
- There is a collection-level test script that, on `401 Unauthorized`, can:
  - Request an access token from `{{url}}/oauth/token` (password/owner credentials style).
  - Store `oauth_token` and `refresh_token` in collection variables.

---

## Top-Level Requests (no folder)

These are root-level requests in the collection.

### 1. Well-know openid configuration
- `GET {{URL}}/.well-known/openid-configuration`
- Returns OpenID Provider configuration (authorization, token, jwks endpoints, etc.).
- Use this to discover endpoints and capabilities of the authorization server.

### 2. OAuth2 JWKs Local
- `GET {{URL}}/oauth2/jwks`
- Returns JSON Web Key Set (JWKS) used to validate JWT signatures.
- Useful for verifying tokens server-side or in debugging tools.

### 3. Device Verification (Standalone)
- `GET https://localhost:8443/api/v1/oauth2/device_verification`
- Corresponds to the device verification page where users approve device login.
- In normal flows, users visit this URL from a browser, not Postman.

---

## OAuth2 (Folder)

This is the main entry point for authentication flows.

### OAuth2 – Single Request

#### Access Token
- `POST {{URL}}/oauth2/token`
- Basic token endpoint example (grant type may vary).
- Used mainly for testing and generic scenarios.

### Subfolders under OAuth2

1. **Web/Mobile Apps**
2. **Device Client (TV / IoT)**
3. **Microservice Client (JWT Bearer)**
4. **Machine-to-Machine Client (Client Credentials)**

Each subfolder demonstrates a specific OAuth2/OIDC flow.

---

## OAuth2 → Web/Mobile Apps → Authentication Code Flow

This folder shows OAuth2 **Authorization Code + PKCE** flows for web/mobile apps.

### 1. Generate PKCE Codes
- `GET {{URL}}/generate_pkce`
- Generates a `code_verifier` and `code_challenge` for PKCE.
- Use these values in your authorization and token requests.

### 2. Request Authorize Code
- Multiple variations exist (e.g., `Request Authorize Code` with different URLs and providers like Facebook/Google).
- Example:
  - `GET {{URL}}/oauth2/authorize?response_type=code&client_id=web-app&redirect_uri=http://127.0.0.1:8080/login/oauth2/code/web-app&scope=openid profile email ...&code_challenge={{code_challenge}}&code_challenge_method=S256&state={{state}}`
- Opens the consent/login page for the user.
- After login, the server redirects with a `code` parameter.

### 3. Access Token (Authorization Code)
- `POST {{URL}}/oauth2/token`
- Exchanges `code` + `code_verifier` for:
  - `access_token`
  - `refresh_token`
  - `id_token` (if OpenID scope used)

### 4. Refresh Token
- `POST {{URL}}/oauth2/token`
- `grant_type=refresh_token`
- Exchanges `refresh_token` for a new access token.

### 5. Revoke Token
- `POST {{URL}}/oauth2/revoke`
- Revokes access or refresh tokens.

### 6. User info
- `GET {{URL}}/userinfo`
- Returns user profile info for the logged-in user (requires valid access token).

---

## OAuth2 → Device Client (TV / IoT)

Implements the **Device Authorization Grant (device code flow)** for TVs/IoT devices.

### 1. Request Device Code
- `POST {{URL}}/oauth2/device_authorization`
- Body (x-www-form-urlencoded):
  - `client_id`: device client ID
  - `scope`: e.g. `openid profile read`
- Response:
  - `device_code`
  - `user_code`
  - `verification_uri` / `verification_uri_complete`
  - `expires_in`, `interval`
- Device shows `user_code` + `verification_uri` to user.

### 2. Access Token (Poll with Device Code)
- `POST {{URL}}/oauth2/token`
- Body:
  - `grant_type=urn:ietf:params:oauth:grant-type:device_code`
  - `device_code`: from step 1
  - `client_id`: same client
- Poll repeatedly (respecting `interval`) until:
  - Success → tokens
  - Or errors (`authorization_pending`, `slow_down`, `expired_token`).

### 3. Refresh Token
- `POST {{URL}}/oauth2/token`
- `grant_type=refresh_token`
- Refreshes the access token for the device client.

### 4. Token Introspect
- `POST {{URL}}/oauth2/introspect`
- Checks validity and metadata of a given token.

### 5. Token Revoke
- `POST {{URL}}/oauth2/revoke`
- Revokes access or refresh tokens for a device client.

---

## OAuth2 → Microservice Client (JWT Bearer)

Shows **JWT Bearer Token** grant for microservice-to-service communication.

### 1. Assertion
- `GET {{URL}}/jwt/assertion`
- Generates or retrieves a JWT assertion that a microservice can use to obtain a token.

### 2. Access Token (JWT Bearer)
- `POST {{URL}}/oauth2/token`
- Uses assertion in body (e.g., `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer`).
- Returns tokens for service-to-service calls.

---

## OAuth2 → Machine-to-Machine Client (Client Credentials)

Demonstrates **Client Credentials Grant** for backend-only services.

### Access Token (Client Credentials)
- `POST {{URL}}/oauth2/token`
- Body:
  - `grant_type=client_credentials`
  - `client_id`, `client_secret`
  - `scope` as needed
- No user context; access token represents the client application itself.

---

## Authentication Code Flow → Token Exchange (Folder)

> Under the “Authentication Code Flow” structure there is a **Token Exchange** folder.

### Access Token (Token Exchange)
- `POST {{URL}}/oauth2/token`
- Used for special token exchange flows (e.g., exchanging one token type for another).
- Exact grant type depends on your backend (e.g. `urn:ietf:params:oauth:grant-type:token-exchange`).

---

## Authentication Code Flow → GitHub / Google / Facebook (Folders)

These folders contain **provider-specific** examples of Authorization Code flows:

### GitHub
- **Request Authorize Code**
- **Access Token**
- **Revoke Token**

### Google
- **Request Authorize Code**
- **Access Token**
- **Revoke Token**

### Facebook
- **Request Authorize Code**
- **Access Token**
- **Revoke Token**

Each folder:
- Shows provider-specific authorization URLs and token endpoints.
- Helps you integrate social login or external OAuth providers.

---

## User (Folder)

Endpoints for managing `User` entities.

### GET All
- `GET {{URL}}/users?size=10&sort=username,asc&page=0`
- Returns a paginated list of users.

### GET One
- `GET {{URL}}/users/{id}`
- Returns a single user by ID.

### CREATE
- `POST {{URL}}/users`
- Creates a new user.
- Body: JSON with user fields (`username`, `password`, roles, etc.).

### UPDATE
- `PUT {{URL}}/users/{id}`
- Updates an existing user.

### DELETE
- `DELETE {{URL}}/users/{id}`
- Deletes a user.

---

## Role (Folder)

Endpoints for managing `Role` entities.

### GET All
- `GET {{URL}}/roles?page=0&size=1&sort=name,asc`

### GET One
- `GET {{URL}}/roles/{id}`

### CREATE
- `POST {{URL}}/roles`

### UPDATE
- `PUT {{URL}}/roles/{id}`

### DELETE
- `DELETE {{URL}}/roles/{id}`

---

## Office (Folder)

Endpoints for managing `Office` entities.

### GET All
- `GET {{URL}}/offices?page=0&size=10&sort=name,asc`

### GET One
- `GET {{URL}}/offices/{id}`

### CREATE
- `POST {{URL}}/offices`

### UPDATE
- `PUT {{URL}}/offices/{id}`

### DELETE
- `DELETE {{URL}}/offices/{id}`

---

## Province (Folder)

Endpoints for managing `Province` entities.

### GET All
- `GET {{URL}}/provinces?page=0&size=10&sort=id,asc`

### GET One
- `GET {{URL}}/provinces/{id}`

### CREATE
- `POST {{URL}}/provinces`

### UPDATE
- `PUT {{URL}}/provinces/{id}`

### DELETE
- `DELETE {{URL}}/provinces/{id}`

---

## District (Folder)

Endpoints for managing `District` entities.

### GET All
- `GET {{URL}}/districts?page=0&size=10&sort=id,asc`

### GET One
- `GET {{URL}}/districts/{id}`

### CREATE
- `POST {{URL}}/districts`

### UPDATE
- `PUT {{URL}}/districts/{id}`

### DELETE
- `DELETE {{URL}}/districts/{id}`

---

## Commune (Folder)

Endpoints for managing `Commune` entities.

### GET All
- `GET {{URL}}/communes?page=0&size=10&sort=id,asc`

### GET One
- `GET {{URL}}/communes/{id}`

### CREATE
- `POST {{URL}}/communes`

### UPDATE
- `PUT {{URL}}/communes/{id}`

### DELETE
- `DELETE {{URL}}/communes/{id}`

---

## Village (Folder)

Endpoints for managing `Village` entities.

### GET All
- `GET {{URL}}/villages?page=0&size=10&sort=id,asc`

### GET One
- `GET {{URL}}/villages/{id}`

### CREATE
- `POST {{URL}}/villages`

### UPDATE
- `PUT {{URL}}/villages/{id}`

### DELETE
- `DELETE {{URL}}/villages/{id}`

---

## File (Folder)

Endpoints for managing file uploads and downloads (user/office).

### Upload
- `POST {{URL}}/files/office/{officeId}`
- Upload file associated with an office.

### Delete
- `DELETE {{URL}}/files/user/{userId}`
- Deletes a user file.

### GET URL
- `GET {{URL}}/files/user/{userId}/url`
- Returns a URL to access the user file.

### GET InputStream
- `GET {{URL}}/files/user/{userId}/inputstream`
- Streams file content.

### GET Base64
- `GET {{URL}}/files/user/{userId}/base64`
- Returns file as Base64-encoded string.

### GET Byte
- `GET {{URL}}/files/user/{userId}/byte`
- Returns file as raw bytes.

---

## How to Use This Guide

1. **Read this `postman.md`** to understand the structure.
2. **Open the same folders in Postman** – the folder and request names match.
3. **Use OAuth2 flows** (in `OAuth2` folder) to obtain tokens.
4. **Call business APIs** (`User`, `Role`, `Office`, `Province`, `District`, `Commune`, `Village`, `File`) with your Bearer token.
5. Refer to the built-in request bodies and examples in Postman for exact JSON shapes.

---

## Suggested Workflow for New Developers

1. Start the backend service locally.
2. Set `{{URL}}` in your Postman environment.
3. Perform an OAuth2 flow:
   - For web app → `OAuth2 → Web/Mobile Apps`.
   - For TV/IoT → `OAuth2 → Device Client (TV / IoT)`.
   - For backend services → `OAuth2 → Machine-to-Machine Client (Client Credentials)` or `Microservice Client (JWT Bearer)`.
4. Once you got `access_token`, test:
   - CRUD endpoints in `User`, `Role`, `Office`, etc.
   - File upload/download in `File`.
5. Explore advanced flows like social login (GitHub/Google/Facebook) in the respective folders.
