# EHS Pro master API

Java 21 / Spring Boot implementation of the original 19 endpoints, extended using `source/Master tables.xlsx` and `source/RBAC.docx`. Includes validated master fields, organizational-unit scopes, customer roles, predefined business permissions, employee-backed system accounts, and Liquibase migrations. Business Unit means Organizational Unit throughout.

See [master fields, RBAC, and account setup](database/master-fields-rbac.md) for the workbook mapping, new APIs, and required reference-data setup.

## Run with MySQL

Requires Java 21, Maven 3.9+, and MySQL 8 on localhost:3306.

```powershell
mvn clean verify
mvn spring-boot:run
```

The default profile is now `mysql`: database **ehs_db**, database username **root**, database password **root**. The JDBC URL creates the database if missing. Liquibase then creates the tables and records migrations; Hibernate validates the schema without altering it. The server binds to `127.0.0.1:8080`.

Backend Super Admin HTTP Basic credentials are separate from database credentials: **ehs-api / ehs-api-local** by default. Override them with `API_USERNAME` and `API_PASSWORD`. Customer system users authenticate with their assigned usernames and BCrypt-protected passwords. Configure reference dropdowns before creating organizations, then create their related records. IDs in the source documents are examples, not seeded geography data.

Swagger UI: <http://localhost:8080/swagger-ui/index.html>. OpenAPI JSON: <http://localhost:8080/v3/api-docs>.

Build an executable JAR with `mvn package`, then run `java -jar target/ehspro-api-1.0.0.jar`.

## Database configuration and migrations

Defaults are in `src/main/resources/application-mysql.yml`. Environment overrides are optional:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'root'
$env:API_USERNAME = 'ehs-api'
$env:API_PASSWORD = 'ehs-api-local'
mvn spring-boot:run
```

`DB_URL` can override the complete JDBC URL. `database/create-database.sql` optionally creates `ehs_db` in MySQL Workbench before startup. Do not run the table migrations manually: Liquibase executes them and maintains their history.

- `src/main/resources/db/changelog/db.changelog-master.yaml` orders the migrations.
- `changes/001-initial-schema.sql` defines all 12 entity/collection tables, primary keys, unique constraints, and relationships. `${largeTextType}` resolves to MySQL `LONGTEXT` (H2 `CLOB` for tests), matching JSON converters.
- `changes/002-list-query-indexes.sql` adds eight indexes as a separate migration.
- `changes/003-master-fields-rbac.sql` aligns master fields and adds reference data, contractors, accounts, role grants, and scopes.
- `changes/004-permission-catalog.sql` seeds business permissions and the immutable system Super Admin role. The final schema has 19 application tables plus 2 Liquibase tables, with 33 changesets.
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

The document only supplies empty `filters` and `multiSortMeta`. Nonempty filters use `{ "field": "name", "matchMode": "contains", "value": "Ops" }`; multisort uses `{ "field": "name", "order": 1 }` (1 ascending, -1 descending). Supported match modes: `contains`, `startsWith`, `endsWith`, `equals`, `notEquals`. Null values support equality modes. Fields must be stored scalar properties. Free-text `filter` searches the main name/description field (employee first name, equipment UID). `totalCount` is calculated after filtering, before pagination.

Errors use the same envelope: HTTP 400 for invalid requests, 401 for missing/invalid credentials, 403 for denied actions or organizational scope, 404 for missing records, 409 for duplicate employee numbers or equipment UIDs within an organization, and 500 for unexpected failures. Creates accept ID 0 or omitted ID and generate IDs. Nested records are persisted transactionally. Employee DTOs return `password: null`; account activation stores only BCrypt hashes in the separate account table.

### Decisions where the document is incomplete

- Missing HTTP methods are inferred: role listing is GET; activity creation and observation listing are POST.
- `GetUsers` lists active employees/contract employees with enabled system accounts. `GetAllEmployees` retains the employee master view; a separate contract-employee list is also available.
- `userId`, `createdBy`, and `modifiedBy` are not authorization claims. Permissions come from the authenticated account's roles, and organizational access comes from separately assigned scopes. See the RBAC guide for the Super Admin restrictions.
- `isExportToExcel: true` returns all matching JSON records, up to 10,000, without pagination. No spreadsheet download contract is provided.
- Geography, language, time-zone, and landing-page catalogs are configurable through the lookup APIs. No complete geography data or image-upload provider was supplied. Unknown equipment/category display names remain null. Images/attachments retain metadata; shift timings are typed and validated.
- Organization parents, employee department/designation/role memberships, supervisors, and business units must exist. Department/designation must belong to the employee's primary organization. Parent and child organizations must have the same tenant.
- Translations are typed JSON values; sublocations and observation subtypes are relational entities. Authorization grants and organizational scopes are relational tables, separate from legacy JSON fields. Subtype translations without IDs match child order.
- PUT endpoints support master editing. No deletion workflows were specified. The catalog and backend system role are seeded; live smoke-test records are explicitly named `EHS-SMOKE-` and retained for inspection.

## Code layout and tests

`controller` handles HTTP; `dto` defines contracts; `service` owns transactions, mappings, reference checks, and queries; `entity` defines persistence; `repository` supplies Spring Data DAOs; `model` holds nested values; `persistence` contains JSON converters; `config` and `exception` configure security and errors.

`MasterApiIntegrationTest` exercises the original contracts, persistence, filtering, validation, and rollback. `RbacIntegrationTest` checks customer roles, cross-instance and organizational scope isolation, denied privilege escalation, contract employees, and workbook fields. `SecurityIntegrationTest` checks authentication. Default tests migrate isolated H2 databases; `LiquibaseMigrationIntegrationTest` checks history/checksums and migration idempotency.

Opt in to actual MySQL verification (applies migrations to the configured database; inserted test records are rolled back):

```powershell
$env:MYSQL_MIGRATION_TEST = 'true'
mvn verify
Remove-Item Env:MYSQL_MIGRATION_TEST
```

For real HTTP verification against a running instance:

```powershell
python scripts/smoke_apis.py --base-url http://127.0.0.1:8080
```

The script calls every OpenAPI operation, checks expected success/error responses, and writes [a summary](reports/api-smoke-results.md) and [full request/response bodies](reports/api-smoke-results.json). Passwords are redacted. It creates synthetic reference data and retains named test records.
