# Spring Boot OAuth2 Postman Collection for iSLDevs

Test OAuth2 flows for iSLDevs (running at `https://localhost:8443`). Import into Postman and set environment variables:
- `{{base_url}}`: `https://localhost:8443`
- `{{client_id}}`: Your client ID (e.g., `web-app`)
- `{{client_secret}}`: Your client secret (for confidential clients)
- `{{redirect_uri}}`: `https://localhost:3000/callback`
- `{{scope}}`: `openid profile email`
- `{{code_verifier}}`: Random string (43-128 chars; generate via pre-request script)

Pre-request script for auth headers:
```javascript
pm.environment.set("auth_header", btoa(pm.environment.get("client_id") + ":" + pm.environment.get("client_secret")));
```

## 1. Authorization Code + PKCE (Web/Mobile Apps)
Secure flow for public clients.

### Step 1: Authorization Request
- **Method**: GET
- **URL**: `{{base_url}}/oauth2/authorize?response_type=code&client_id={{client_id}}&redirect_uri={{redirect_uri}}&scope={{scope}}&code_challenge={{code_challenge}}&code_challenge_method=S256&state=xyz123`
- **Headers**: None
- **Body**: None
- **Notes**: Logs in/approves scopes. Copy `code` from redirect URL. Generate challenge:
  ```javascript
  const verifier = crypto.randomUUID().replace(/-/g, '');
  pm.environment.set("code_verifier", verifier);
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const digest = await crypto.subtle.digest('SHA-256', data);
  pm.environment.set("code_challenge", btoa(String.fromCharCode(...new Uint8Array(digest))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, ''));
  ```

### Step 2: Token Request
- **Method**: POST
- **URL**: `{{base_url}}/oauth2/token`
- **Headers**:
    - `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
    - `grant_type`: `authorization_code`
    - `code`: `<pasted code>`
    - `redirect_uri`: `{{redirect_uri}}`
    - `code_verifier`: `{{code_verifier}}`
- **Notes**: Gets `access_token` and `refresh_token`. Test with `Authorization: Bearer {{access_token}}`.

## 2. Client Credentials (Machine-to-Machine)
For service accounts.

### Token Request
- **Method**: POST
- **URL**: `{{base_url}}/oauth2/token`
- **Headers**:
    - `Authorization: Basic {{auth_header}}`
    - `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
    - `grant_type`: `client_credentials`
    - `scope`: `api.internal monitoring.read`
- **Notes**: Gets `access_token` for APIs. No user login needed.

## 3. JWT Bearer (Microservices)
Exchange external JWT for local token. Uses keys from `jwt_keys` table.

### Token Request
- **Method**: POST
- **URL**: `{{base_url}}/oauth2/token`
- **Headers**:
    - `Authorization: Basic {{auth_header}}`
    - `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
    - `grant_type`: `urn:ietf:params:oauth:grant-type:jwt-bearer`
    - `assertion`: `<signed JWT (e.g., from jwt.io with iss={{base_url}}, sub=microservice, aud={{base_url}}/oauth2/token)>`
    - `client_id`: `microservice`
- **Notes**: JWT claims: `iss`, `sub` (client_id), `aud` (token endpoint), `exp`, `iat`. Sign with RS256 using private key from `jwt_keys`.

## 4. Device Code (IoT/TV)
For input-limited devices.

### Step 1: Device Authorization
- **Method**: POST
- **URL**: `{{base_url}}/oauth2/device_authorization`
- **Headers**:
    - `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
    - `client_id`: `iot-device`
    - `scope`: `openid device.manage`
- **Notes**: Gets `device_code`, `user_code`, `verification_uri`. User visits URI, enters code.

### Step 2: Poll for Token
- **Method**: POST
- **URL**: `{{base_url}}/oauth2/token`
- **Headers**:
    - `Authorization: Basic {{auth_header}}`
    - `Content-Type: application/x-www-form-urlencoded`
- **Body** (x-www-form-urlencoded):
    - `grant_type`: `urn:ietf:params:oauth:grant-type:device_code`
    - `device_code`: `<from Step 1>`
- **Notes**: Poll every `interval` seconds until user approves.

## 5. Social Logins (GitHub/Google)
Uses OAuth2 client registration. Client IDs/secrets stored in `config` table.

### GitHub Login
#### Authorization Request
- **Method**: GET
- **URL**: `{{base_url}}/oauth2/authorization/github?redirect_uri={{redirect_uri}}&scope=user:email`
- **Headers**: None
- **Body**: None
- **Notes**: Redirects to GitHub. Spring handles token exchange.

#### User Info
- **Method**: GET
- **URL**: `{{base_url}}/userinfo`
- **Headers**:
    - `Authorization: Bearer {{access_token}}`
- **Notes**: Gets mapped user profile.

### Google Login
#### Authorization Request
- **Method**: GET
- **URL**: `{{base_url}}/oauth2/authorization/google?redirect_uri={{redirect_uri}}&scope=openid email profile`
- **Headers**: None
- **Body**: None
- **Notes**: Redirects to Google. Supports OpenID.

#### User Info
- **Method**: GET
- **URL**: `{{base_url}}/userinfo`
- **Headers**:
    - `Authorization: Bearer {{access_token}}`
- **Notes**: Fetches profile claims.

## Tips
- Test protected endpoints: GET `{{base_url}}/api/user` with `Authorization: Bearer {{access_token}}`.
- Errors: Check `invalid_grant`, `insufficient_scope`. Enable `logging.level.org.springframework.security=DEBUG`.
- CORS: Add `@CrossOrigin` for Postman testing.
- Import: Create a Postman collection named "iSLDevs OAuth2" with these requests.