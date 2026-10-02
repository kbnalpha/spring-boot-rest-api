# EHS Pro master API

Java 21 / Spring Boot implementation of all master API contracts in `source/all master api with response.docx`, including contractor, contract-employee, and temporary-user workflows, extended using `source/Master tables.xlsx` and `source/RBAC.docx`. Includes validated master fields, organizational-unit scopes, customer roles, predefined business permissions, employee-backed system accounts, and Liquibase migrations. Business Unit means Organizational Unit throughout.

Test with the ordered [IntelliJ HTTP collection](docs/master-api-tests.http), or copy URLs and JSON from [API test payloads](docs/api-test-payloads.md). The HTTP collection creates sample reference data and captures generated IDs. Configure SMTP and run through activation; read the emailed temporary password, fill the temporaryPassword variable, then continue. See [account onboarding](docs/account-onboarding.md).

See [master fields, RBAC, and account setup](database/master-fields-rbac.md) for the workbook mapping, new APIs, and required reference-data setup.

## Run with MySQL

### Browser access (CORS)

API routes under `/api/**` accept cross-origin browser requests from the exact origins in `CORS_ALLOWED_ORIGINS`. Defaults support local frontends on `localhost` and `127.0.0.1`, ports `3000` and `5173`. On Render, set the variable in **Environment** and redeploy, for example:

```properties
CORS_ALLOWED_ORIGINS=https://your-frontend.example.com,http://localhost:3000,http://localhost:5173
```

Use the **frontend** origin (scheme, hostname, and port), without a path or trailing slash. This list replaces the defaults. Wildcard `*` is rejected because credentialed requests are enabled. Other origins are denied by browser CORS handling; Postman/backend access still depends on authentication and authorization rather than CORS.

Preflight OPTIONS requests for allowed origins are handled before authentication. GET, POST, PUT, PATCH, and DELETE requests support `Authorization`, `Content-Type`, `Accept`, `Accept-Language`, `Accept-Org-Language`, and `Accept-Nav-Language` headers. Business requests require a Bearer JWT (or legacy HTTP Basic credentials) and appropriate permissions; first-login password reset rules remain enforced. Login issues a signed Bearer JWT. No network/IP allowlisting is needed solely because another developer uses a different network.


For container deployment, use the root `Dockerfile` and `render.yaml`. Follow the [Render deployment guide](docs/render-deployment.md) for environment variables, SMTP configuration, health checks, and local Docker commands.

Requires Java 21, Maven 3.9+, and access to the configured Aiven MySQL service. The mysql profile imports the project-root `.env` file; `.env.example` provides a template.

```powershell
mvn clean verify
mvn spring-boot:run
```

The default profile is `mysql`: host **mysql-ehs-kbnalpha-bbce.l.aivencloud.com**, port **15129**, database **ehs_db**, and username **avnadmin**. The password comes from `DB_PASSWORD` in the local Git-ignored `.env` file or the environment. TLS is required (`sslMode=REQUIRED`). The JDBC URL creates the database if missing. Liquibase then creates the tables and records migrations; Hibernate validates the schema without altering it. The server binds to `127.0.0.1:8080`.

Backend Super Admin HTTP Basic credentials are separate from database credentials: **ehs-api / ehs-api-local** by default. Override them with `API_USERNAME` and `API_PASSWORD`. New system users authenticate with their employee email and BCrypt-protected passwords. Admin/Super Admin activation emails a temporary password; a mandatory first-login reset precedes all business access. Configure reference dropdowns before creating organizations, then create their related records. IDs in the source documents are examples, not seeded geography data.

Swagger UI: <http://localhost:8080/swagger-ui/index.html>. OpenAPI JSON: <http://localhost:8080/v3/api-docs>.

Build an executable JAR with `mvn package`, then run `java -jar target/ehspro-api-1.0.0.jar`.

## Database configuration and migrations

Defaults are in `src/main/resources/application-mysql.yml`. Environment overrides are optional:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:DB_USERNAME = 'avnadmin'
# DB_PASSWORD is loaded from .env; set it in your deployment environment when deployed.
$env:API_USERNAME = 'ehs-api'
$env:API_PASSWORD = 'ehs-api-local'
mvn spring-boot:run
```

`DB_HOST`, `DB_PORT`, `DB_NAME`, and `DB_USERNAME` can override individual connection settings; `DB_URL` can override the complete JDBC URL. API URLs and API authentication remain unchanged. Environment variables override `.env` values; restart the app after configuration changes. `database/create-database.sql` optionally creates `ehs_db` in MySQL Workbench before startup. Do not run the table migrations manually: Liquibase executes them and maintains their history.

- `src/main/resources/db/changelog/db.changelog-master.yaml` orders the migrations.
- `changes/001-initial-schema.sql` defines all 12 entity/collection tables, primary keys, unique constraints, and relationships. `${largeTextType}` resolves to MySQL `LONGTEXT` (H2 `CLOB` for tests), matching JSON converters.
- `changes/002-list-query-indexes.sql` adds eight indexes as a separate migration.
- `changes/003-master-fields-rbac.sql` aligns master fields and adds reference data, contractors, accounts, role grants, and scopes.
- `changes/004-permission-catalog.sql` seeds business permissions and the immutable system Super Admin role. Migration 005 extends this catalog for delegated role administration.
- `changes/005-complete-master-contract.sql` adds contractor contact/address fields, temporary users and memberships, and selectable admin permissions. The final schema has 22 application tables plus 2 Liquibase tables, with 41 changesets.
- `changes/006-source-permission-catalog.sql` adds all source permission definitions and maps 129 source IDs/names to canonical permissions without changing existing grants.
- `changes/007-built-in-admin.sql` adds the protected built-in Admin role. Admin receives all actions within its assigned organization tree, including future descendants; root creation remains Super Admin-only.
- `changes/008-account-onboarding.sql` adds mandatory first-login password state and expiry, and expands usernames for email addresses.
- `changes/009-required-primary-keys.yaml` adds primary keys to Liquibase history and employee organization membership. A dedicated Liquibase connection bootstraps Aiven migrations; normal application connections retain the server primary-key requirement.
- `DATABASECHANGELOG` stores each applied changeset's ID, author, filename, execution time, order, and checksum. `DATABASECHANGELOGLOCK` prevents concurrent migration runs.
- `spring.jpa.hibernate.ddl-auto=validate` and `spring.sql.init.mode=never` leave schema writes to Liquibase. Do not switch Hibernate to `update`.

For future changes, update the entity, add a **new** numbered SQL changeset, and include the file at the end of the master changelog. Restarting the application applies only pending changes. Applied changesets are immutable: append a new changeset rather than editing old SQL or clearing checksums. See [the migration guide](database/README.md) for an `ALTER TABLE` example and history queries. Liquibase tracks changes made through migrations; it does not automatically capture SQL run manually in Workbench or generate migrations from entity edits.

The optional H2 development profile is available with `mvn spring-boot:run "-Dspring-boot.run.profiles=local"`. It uses the same migrations in `./data/ehspro-liquibase`. Authentication and RBAC apply in every profile. Do not combine `local` and `mysql` profiles.

## API contract

Successes return HTTP 200 and `{ "statusCode": 200, "message": "Successful", "results": ... }`. Paged lists return `{ "items": [...], "totalCount": N }` inside `results`.

| Method | Path | Success results |
| --- | --- | --- |
| GET | `/api/OrganizationUnit/GetAllOrganizations` | Organization tree with `children` |
| POST | `/api/OrganizationUnit` | `"Organization Unit created successfully."` |
| POST | `/api/User/GetUsers` | Paged users |
| POST | `/api/User/GetAllEmployees` | Paged employees |
| POST | `/api/User/CreateEmployee` | Numeric employee ID |
| POST | `/api/Location/GetAllLocations` | Paged locations |
| POST | `/api/Location/add` | Numeric location ID |
| GET | `/api/Role/GetAllRoles` | Roles array |
| POST | `/api/Role/CreateRole` | `{ "id": N, "message": "Role created successfully." }` |
| POST | `/api/OperationActivity` | `{ "id": N }` |
| POST | `/api/OperationActivity/list` | Paged activities |
| POST | `/api/ObservationType/Create` | Created observation type and subtypes |
| POST | `/api/ObservationType/GetAll` | Paged observation types |
| POST | `/api/Equipment/List` | Paged equipment |
| POST | `/api/Equipment/Add` | `"Equipment added successfully."` |
| POST | `/api/Designation/GetList` | Paged designations |
| POST | `/api/Designation/Create` | `{ "id": N }` |
| POST | `/api/Department/GetList` | Paged departments |
| POST | `/api/Department/Create` | `{ "id": N }` |
| POST | `/api/Contractor/GetAllContractors` | Paged contractors with contact/address details |
| POST | `/api/Contractor/Add` | `"Contractor added successfully."` |
| POST | `/api/User/GetAllContractEmployees` | Paged contract employees |
| POST | `/api/User/CreateEmployee` with `userType: 2` | Numeric contract-employee ID |
| POST | `/api/User/GetAllExternalCollabarator` | Paged temporary users |
| POST | `/api/User/CreateOrUpdateExternalCollabarator` | Numeric temporary-user ID; positive `id` updates |

Example workflow after importing the referenced country/state/city/language/time-zone IDs using the lookup APIs:

```powershell
$base = 'http://localhost:8080/api'
$headers = @{ Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes('ehs-api:ehs-api-local')) }
Invoke-RestMethod "$base/OrganizationUnit" -Headers $headers -Method Post -ContentType 'application/json' -Body '{"name":"Head Office","tenantId":1,"status":1,"line1":"Office address","country":101,"state":123,"city":456,"languageID":1,"timeZoneID":16,"keyContactName":"Contact","phoneNumber":"9000000000","emailAddress":"contact@example.com"}'
$tree = Invoke-RestMethod "$base/OrganizationUnit/GetAllOrganizations" -Headers $headers
$organizationId = $tree.results[0].id
$body = @{ name = 'Operations'; businessUnitId = $organizationId; status = 1 } | ConvertTo-Json
Invoke-RestMethod "$base/Department/Create" -Headers $headers -Method Post -ContentType 'application/json' -Body $body
$query = @{ businessUnitIds = "$organizationId"; skipCount = 0; maxResultCount = 10; sorting = 'name'; sortingType = 'asc' } | ConvertTo-Json
Invoke-RestMethod "$base/Department/GetList" -Headers $headers -Method Post -ContentType 'application/json' -Body $query
```

### Lists and validation

`skipCount` is a zero-based row offset. `maxResultCount` defaults to 10 and accepts 1–1000. `businessUnitIds` accepts comma-separated positive IDs; employee filtering includes secondary memberships. `id` optionally selects one record. Empty sorting defaults to ascending `id`, and an ID tiebreaker keeps pagination deterministic.

The source documents only supply empty `filters` and `multiSortMeta`. Nonempty filters use `{ "field": "name", "matchMode": "contains", "value": "Ops" }`; multisort uses `{ "field": "name", "order": 1 }` (1 ascending, -1 descending). Supported match modes: `contains`, `startsWith`, `endsWith`, `equals`, `notEquals`. Null values support equality modes. Fields must be stored scalar properties. Free-text `filter` searches the main name/description field (employee first name, equipment UID). `totalCount` is calculated after filtering, before pagination.

Errors use the same envelope: HTTP 400 for invalid requests, 401 for missing/invalid credentials, 403 for denied actions or organizational scope, 404 for missing records, 409 for duplicate employee numbers or equipment UIDs within an organization, and 500 for unexpected failures. Creates accept ID 0 or omitted ID and generate IDs. Nested records are persisted transactionally. Employee DTOs return `password: null`; account activation stores only BCrypt hashes in the separate account table.

### Decisions where the document is incomplete

- Missing HTTP methods are inferred: role listing is GET; activity creation and observation listing are POST.
- `GetUsers` lists active employees/contract employees with enabled system accounts. `GetAllEmployees` retains the employee master view; a separate contract-employee list is also available.
- `userId`, `createdBy`, and `modifiedBy` are not authorization claims. Permissions come from the authenticated account's roles, and organizational access comes from separately assigned scopes. See the RBAC guide for the Super Admin restrictions.
- `isExportToExcel: true` returns all matching JSON records, up to 10,000, without pagination. No spreadsheet download contract is provided.
- Geography, language, time-zone, and landing-page catalogs are configurable through the lookup APIs. No complete geography data or image-upload provider was supplied. Unknown equipment/category display names remain null. Images/attachments retain metadata; shift timings are typed and validated.
- Organization parents, employee department/designation/role memberships, supervisors, and business units must exist. Department/designation must belong to the employee's primary organization. Parent and child organizations must have the same tenant.
- Translations are typed JSON values; sublocations and observation subtypes are relational entities. Authorization grants and organizational scopes are relational tables, separate from legacy JSON fields. Subtype translations without IDs match child order.
- PUT endpoints support master editing. Temporary users use their documented create-or-update POST. No deletion workflows were specified. The catalog and backend system role are seeded; live smoke-test records are explicitly named `EHS-SMOKE-` and retained for inspection.

## Code layout and tests

`controller` handles HTTP; `dto` defines contracts; `service` owns transactions, mappings, reference checks, and queries; `entity` defines persistence; `repository` supplies Spring Data DAOs; `model` holds nested values; `persistence` contains JSON converters; `config` and `exception` configure security and errors.

`MasterApiIntegrationTest` exercises the original contracts, persistence, filtering, validation, and rollback. `RbacIntegrationTest` checks customer roles, cross-instance and organizational scope isolation, denied privilege escalation, contract employees, and workbook fields. `SecurityIntegrationTest` checks authentication. Default tests migrate isolated H2 databases; `LiquibaseMigrationIntegrationTest` checks history/checksums and migration idempotency.

Opt in to actual MySQL verification (applies migrations to the configured database; inserted test records are rolled back):

```powershell
$env:MYSQL_MIGRATION_TEST = 'true'
mvn verify
Remove-Item Env:MYSQL_MIGRATION_TEST
```

For real HTTP verification against a running instance, first start the local SMTP inbox with `python scripts/smtp_test_sink.py` and configure the app to use localhost:1025. The smoke script uses only synthetic email addresses:

```powershell
python scripts/smoke_apis.py --base-url http://127.0.0.1:8080
```

The script calls every OpenAPI operation, checks expected success/error responses, and writes [a summary](reports/api-smoke-results.md) and [full request/response bodies](reports/api-smoke-results.json). Passwords are redacted. It creates synthetic reference data and retains named test records.

## Dynamic roles and temporary users

Admins select `permissionIds` from `GET /api/Permission/GetAll`. Grant `CreateRole` (3302) to create roles, `ManageRoles` (3272) to edit roles, and `ManageRoleUsers` (3274) to assign basic/additional roles to system users. Custom roles can have any non-reserved name; Admin and Super Admin are protected built-in roles. Role definitions stay within the admin's tenant; user assignment also checks the target employee's organizational scope. Super Admin can perform all these actions. The built-in Admin can also activate/deactivate accounts and assign scope within its own tree; Super Admin can do so across instances.

Temporary users require `ManageExternalCollaborators` (3357); creation or a role change additionally requires `ManageRoleUsers`. Their `roleId` must refer to an active, non-system role in the same tenant. `externalDetails` company/designation/status are persisted and returned. `hasAccess` is source metadata: temporary-user creation does not provision credentials or bypass the employee-backed account activation model. Passwords are always returned as null.

The exact source spelling `Collabarator` is preserved in routes. Existing `/api/Contractor/Create`, `/api/Contractor/GetList`, and `/api/ContractEmployee/*` endpoints remain available. Contractor `name` remains a compatible alias for `contractorName`; conflicting names are rejected. Geography IDs must exist and agree. Source IDs and display values are examples; generated IDs and persisted values are returned.

See [Admin setup and child-organization examples](docs/admin-role.md) for assigning the built-in Admin role.

Account types are SUPER_ADMIN, ADMIN, and USER. The new `/api/Auth/authenticate`, `/api/Auth/FirstLoginPasswordReset`, and `/api/SystemUser/{id}/ResendActivation` APIs implement email onboarding without UI code. Activation no longer accepts a username or password. See [the complete flow and SMTP configuration](docs/account-onboarding.md).

Frontend clients may send all three language headers:

```javascript
config.headers["Accept-Language"] = defaultLanguage;
config.headers["Accept-Org-Language"] = orgLanguage;
config.headers["Accept-Nav-Language"] = language;
```

Authenticate with `POST /api/Auth/authenticate` and JSON `{"username":"employee@example.com","password":"your-password"}`. This replaces `/api/Auth/Login`; update frontend callers. These headers are allowed by CORS but do not change response localization by themselves.

## Authentication response

`POST /api/Auth/authenticate` returns the standard `statusCode`, `message`, and `results` envelope. Results now include `id` (system-account ID), `organizationUnitId` (employee primary organization), `userName` (display name), `email`, `token`, `roles`, `permissions`, `landingPage`, `resetPassword`, `userType`, `contractorCompanyId`, `clientId` (tenant), `languageCode`, and `buLanguageCode`.

Permissions use frontend names such as `BusinessUnit(BU).ManageEmployees` and `Administration.ManageRoles`, mapped from the existing permission catalog and source aliases. Parent names are included with granted children. Admin/Super Admin receive the full catalog; users receive only their effective grants. These display strings do not change backend permission checks or organization scope. Catalog entries for future modules do not imply that module APIs are implemented.

`token` is a signed JWT; send `Authorization: Bearer <token>`. `authenticationType` is `BEARER` and `expiresIn` is the lifetime in seconds. HTTP Basic remains supported for existing clients. Existing `username` (login identifier), `accountType`, `authenticationType`, `mustChangePassword`, `nextAction`, and conditional `resetPasswordEndpoint` fields remain available. Pending first-login users receive `resetPassword: true` and an empty permissions list until reset.

The configured Super Admin uses reserved ID `-1` (not an employee/account table ID), `email` from `SUPER_ADMIN_EMAIL` (default `kbnalpha@gmail.com`), null `organizationUnitId`, and system `clientId: 0`. Employees use their real system-account IDs and email addresses. Both `languageCode` and `buLanguageCode` are `en-US` as required by the client contract. Non-contract employees have `contractorCompanyId: 0`.

### JWT deployment

Set `JWT_SECRET` to a base64-encoded random key of at least 32 bytes in Render. Blueprint sync generates it when absent; a manually created service needs the variable added manually. Keep the same key across instances and deployments. Never use an API password or SMTP key for JWT signing. `JWT_TTL_SECONDS` defaults to 3600 (allowed 60?86400). Development without a key uses an ephemeral key, invalidating tokens on restart.

Tokens validate signature, issuer and expiry, and reload the account/roles for each request. Disabled/deleted accounts cannot use their tokens; password reset or resend invalidates previous tokens. Pending first-login tokens can only authenticate/reset the password, not access business APIs. After reset, authenticate again for a new token. There is no refresh-token endpoint; authenticate again when expired. Configured Super Admin tokens are invalidated when its configured password changes.

```http
GET /api/Auth/Me
Authorization: Bearer <results.token>
```

## User detail and language APIs

- `GET /api/Common/getLanguages`: public static response: `{"statusCode":200,"message":"Successful","results":[{"id":1,"name":"English (US)","code":"en-US"}]}`. This UI language ID is independent of the database `LANGUAGE` lookup IDs used for organization/employee writes.
- `GET /api/User/{id}`: authenticated employee detail, with `password: null` and `organizationUnitIdsMapped` represented as `[{"organizationUnitId":123,"isChecked":true}]`. Other fields come from the stored employee and related masters. Write payloads continue to use numeric organization-ID arrays.
- `GET /api/User/GetUserOrganizationUnit/{id}`: organization memberships with `userId`, `buImage`, `organizationUnitId`, `organizationUnitName`, `isAnonymous`, `isObservationProofRequired`, `languageId`, and `currency`.

Both user routes use **employee IDs**, not the account ID returned by authenticate. An ordinary user can read their own employee profile. Reading another employee requires the corresponding employee-management permission plus organization scope; Admin/Super Admin keep elevated access. Membership organization details enforce account scope. These endpoints never grant additional access. Missing employees return 404; disallowed reads return 403. The configured Super Admin is not an employee; its reserved login ID `-1` has no employee detail record.
