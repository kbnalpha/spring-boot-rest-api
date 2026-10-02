# Employee activation and first login APIs

Account types are `SUPER_ADMIN`, `ADMIN`, and `USER`. Super Admin is the configured backend identity. An active built-in Admin role gives organization-scoped administration. All other system accounts are Users, whose actions come from their assigned roles and permissions. `GET /api/Auth/Me` and `POST /api/Auth/authenticate` expose the effective account type.

No UI is implemented. APIs return a `nextAction` value for the caller to decide what to show; they do not issue browser redirects. Authentication remains stateless HTTP Basic. Login validates credentials and reports the next step; it does not issue a session cookie or token. Use HTTPS when deploying.

## Configure email

The application sends through SMTP. Supply your provider's settings as environment variables; do not commit the SMTP password:

```powershell
$env:SMTP_HOST = 'smtp.your-provider.example'
$env:SMTP_PORT = '587'
$env:SMTP_USERNAME = 'your-smtp-username'
$env:SMTP_PASSWORD = '<set locally>'
$env:SMTP_FROM = 'no-reply@your-company.example'
$env:SMTP_AUTH = 'true'
$env:SMTP_STARTTLS = 'true'
$env:TEMP_PASSWORD_HOURS = '24'
mvn spring-boot:run
```

Use your provider's actual values. Temporary passwords expire after 24 hours by default; the supported configuration range is 1–168 hours. For local testing, SMTP defaults to localhost:1025 without authentication/TLS. Connection/read/write timeouts are five seconds.

If SMTP rejects the message or is unavailable, activation/resend returns HTTP 503 and rolls back its account changes. SMTP acceptance is not a guarantee of inbox delivery; provider bounces are outside this API. A rare database commit failure after SMTP acceptance can also require the Admin to retry activation/resend.

## 1. Admin enables an employee as a User

The employee must be active and have a valid, unique email for their login. Both regular employees and contract employees are supported. Authenticate as Admin or Super Admin:

```http
POST /api/User/125/ActivateSystemUser
Content-Type: application/json

{
  "basicRoleId": 3,
  "additionalRoleIds": [],
  "scopes": [
    { "organizationUnitId": 100, "includeDescendants": true }
  ]
}
```

Replace example IDs with actual IDs. The API derives the username from `employee.emailAddress`, trims it, and stores it in lowercase. The server generates a random temporary password, persists only its BCrypt hash, marks `mustChangePassword: true`, enables the account, and emails the credentials to that address. Do not submit `username` or `password` in activation requests; those obsolete fields now return HTTP 400.

The response contains account details, username, `mustChangePassword`, and `temporaryPasswordExpiresAt` (UTC). It never contains the temporary password or hash. An employee's `hasAccess` flag alone does not create credentials; use this activation endpoint.

Admin can activate accounts only in its assigned organization tree and grant scopes within that tree. To activate another Admin, select the protected role with `builtInAdmin: true`. Ordinary Users cannot activate accounts or resend activation emails.

## 2. Employee logs in using the email credentials

No Authorization header is needed for the login endpoint:

```http
POST /api/Auth/authenticate
Content-Type: application/json

{
  "username": "employee@example.com",
  "password": "TEMPORARY_PASSWORD_FROM_EMAIL"
}
```

HTTP 200, abbreviated response:

```json
{
  "statusCode": 200,
  "message": "Successful",
  "results": {
    "username": "employee@example.com",
    "accountType": "USER",
    "mustChangePassword": true,
    "nextAction": "RESET_PASSWORD",
    "authenticationType": "HTTP_BASIC",
    "resetPasswordEndpoint": "/api/Auth/FirstLoginPasswordReset"
  }
}
```

Until reset, business APIs, permission catalogs, `/api/Auth/Me`, and OpenAPI access return HTTP 403 with `nextAction: RESET_PASSWORD`. This restriction also applies to newly activated Admin accounts. Expired temporary credentials and disabled/inactive accounts return HTTP 401.

## 3. Set the first permanent password

Authenticate with HTTP Basic using the employee email and temporary password:

```http
POST /api/Auth/FirstLoginPasswordReset
Content-Type: application/json

{
  "currentPassword": "TEMPORARY_PASSWORD_FROM_EMAIL",
  "newPassword": "My-new-password-123",
  "confirmPassword": "My-new-password-123"
}
```

The new password must match its confirmation, differ from the temporary password, contain 12–72 characters, and fit within BCrypt's 72-byte limit. On success the response contains `mustChangePassword: false` and `nextAction: LOGIN`. The temporary password immediately stops working. Concurrent requests cannot reuse the same temporary password to overwrite a completed reset.

## 4. Log in again

Call `/api/Auth/authenticate` with the email and new password. Successful login returns `nextAction: LOGIN_SUCCESS`, `mustChangePassword: false`, account type, and effective authorities. Use that email/new password as HTTP Basic credentials for subsequent API requests. User roles and organizational scope are still enforced.

## Resend an expired or missing activation email

```http
POST /api/SystemUser/10/ResendActivation
```

Admin/Super Admin authentication is required; no payload is needed. This generates and emails a fresh temporary password and invalidates the previous password immediately. It can also restart onboarding for a User who has forgotten their permanent password. The target must be active/enabled and within the Admin's manageable scope.

`PUT /api/SystemUser/10/Enabled` still disables/re-enables an existing account. It does not generate credentials or bypass a pending reset. Use resend when new credentials are needed.

## Existing accounts and email changes

Migration 008 preserves existing accounts and passwords. It does not guess unique emails or force a reset for users created before this workflow. To convert a legacy username, set a valid unique employee email and call `ResendActivation`; the username then becomes that email and password reset becomes mandatory. Editing employee email alone does not silently change an existing login.

## Test without external email delivery

```powershell
python scripts/smtp_test_sink.py
```

This local-only SMTP inbox never forwards messages. Leave it running, start the app with localhost:1025 SMTP settings, then run:

```powershell
python scripts/smoke_apis.py --base-url http://127.0.0.1:8080
```

The smoke script reads synthetic activation emails from `target/smtp-test-messages.jsonl` and completes onboarding. That ignored test file contains temporary test credentials; API reports redact passwords. For manual testing, [master-api-tests.http](master-api-tests.http) pauses logically after activation: read the email, fill `temporaryPassword`, and continue. No development endpoint exposes passwords.
