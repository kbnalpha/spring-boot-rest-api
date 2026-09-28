# Master fields and RBAC implementation

Sources: `source/Master tables.xlsx` and `source/RBAC.docx`. Business Unit and Organizational Unit use the existing `organization_unit` entity and `/api/OrganizationUnit` APIs; there is no duplicate business-unit table.

## Workbook mapping

| Source sheet and rows | Implementation |
| --- | --- |
| Business Unit 2 | `name` is required and limited to 50 characters. Migration 003 narrows the database column and stops rather than truncating existing longer names. |
| Business Unit 3 | Existing `buImage`/attachment metadata is retained; optional `layoutImageUrl`, `latitude`, and `longitude` support a stored image URL or map location. Coordinates must be supplied together and lie within latitude/longitude bounds. The backend stores metadata; an image hosting/upload provider is not specified by the sources. |
| Business Unit 4–13 | Address line 1, country, state, city, language, and time zone are required. Reference checks enforce country/state/city relationships and country-specific language/time-zone choices. Country code, currency, and symbol come from the country reference rather than the submitted values. |
| Business Unit 14–16 | Contact name, phone, and email are required; phone and email formats are validated. |
| Business Unit 17 | `shifts` contains typed `{name,startTime,endTime}` values. Start/end times are required and cannot be equal. Overnight shifts are supported. A PUT replaces the list, allowing shifts to be added or removed. |
| Roles 2–6 | Required name and landing-page reference, optional description, and at least one predefined business permission. Permissions include module identifiers for grouping. Roles belong to an instance (`tenantId`). |
| Employees 2–20 | Required first/last name, gender, unique `userNumber`, organization, department, designation, language, and active status. Existing `status` 1 means active, 2 inactive. Optional date of birth is not in the future; age is calculated when returning the employee. Country code is derived from country. |
| Contract Employees 2–20 | Uses Employee with `userType=2`, the same personal/contact rules, and a required contractor company and designation. Department is optional. Contractors and designations must belong to the selected organization. Regular employees use `userType=1`. |
| System Users 2–6 | Accounts are activated from an existing employee or contract employee. `GetUsers` lists active, enabled accounts attached to those employees. Responses include `systemAccountId`, `systemUsername`, `basicRoleId`, `additionalRoleIds`, and `systemAccountEnabled`. There is no independent create-user API. |

Existing data is retained. Aside from the checked organization-name limit, legacy nullable columns are not blindly made non-null or populated with invented values. New and edited records enforce the workbook requirements at the API boundary. Old incomplete records need their required fields filled before a full PUT can succeed.

## Security model

Every profile now requires authentication, including local H2. HTTP Basic uses BCrypt hashes for database-backed accounts. The configured backend credentials (`API_USERNAME` / `API_PASSWORD`, default `ehs-api` / `ehs-api-local`) represent Super Admin. This identity and its immutable system role are not created, assigned, or edited through application APIs.

Customer roles contain predefined business permissions. A user's required basic role plus optional additional roles determine their available actions. Independently, `account_organization_scope` determines their allowed organizational units. Each scope can include descendants, and additional units can be assigned explicitly. Expansion stays within the user's instance. Disabling an account, disabling its employee, changing its roles, or changing its scope takes effect on the next authenticated request.

`ManageRoles`, `ManageRoleUsers`, and `ManageBusinessUnits` are Super Admin-only and cannot be delegated to a customer role. Permission responses mark these as disabled for selection. Super Admin can perform their corresponding actions through its system authority.

The document's distinction between creation and management is implemented as follows: a customer admin with `CreateRole` can create a customer role; only Super Admin can edit existing roles, assign roles/scopes, or activate/deactivate system accounts. `ViewRoles` grants instance-limited role listing. No predefined customer role named Admin is required. There are no individual permission overrides; the document leaves those for later review, so additional access is granted through additional roles.

Setup actions have business permissions such as `ManageDepartment`, `ManageEmployees`, and `ManageLocations`. Reads are scoped in the database before pagination/counting. Writes validate both the existing record's organization and the submitted organization. Existing master records cannot be moved into another organization through PUT. A request's `userId` or employee membership fields never establish security scope.

The catalog includes Incident business actions (create/view/edit/review/approve/close) and Observation actions. These are predefined permission definitions for those modules; the sources do not specify incident-processing APIs, so no incident workflow is invented.

## Schema changes

Applied migrations 001 and 002 remain unchanged. New migrations are:

- `003-master-fields-rbac.sql`: layout/map fields, organization-name limit, employee country code, instance/system flags on roles, reference data, contractors, permission definitions, role permissions, system accounts, account roles, and account organization scopes.
- `004-permission-catalog.sql`: predefined permissions and the immutable Super Admin role.

Legacy JSON role and employee membership columns remain for compatibility. Authorization reads only the new validated relational grants and account scopes. Existing roles' unverified JSON permissions do not automatically become authorization grants; review/save those roles and explicitly activate accounts. Existing employee `hasAccess` alone does not create credentials or bypass activation.

After these migrations there are 19 application tables and 2 Liquibase history/lock tables, with 33 recorded changesets.

## Reference dropdowns

`GET /api/Lookup/{kind}?countryId=...&stateId=...` returns entries for `COUNTRY`, `STATE`, `CITY`, `LANGUAGE`, `TIME_ZONE`, or `LANDING_PAGE`.

Super Admin can import configured reference entries using `PUT /api/Lookup/{kind}/{id}`. Examples of bodies:

```json
{"name":"India","code":"91","currency":"INR","symbol":"₹"}
```

```json
{"name":"Your state","countryId":101}
```

```json
{"name":"Your city","countryId":101,"stateId":123}
```

```json
{"name":"English","countryId":101}
```

```json
{"name":"India Standard Time","countryId":101,"zoneId":"Asia/Kolkata"}
```

Language entries with no `countryId` are globally available. Time zones require a valid IANA `zoneId`. Reference kinds have separate ID namespaces. The sources contain no complete reference catalog, so geography records are imported rather than fabricated. The Dashboard landing page is seeded with ID 1. Smoke tests create clearly labeled synthetic geography records using IDs 900001–900005.

## Account setup and APIs

1. Authenticate as backend Super Admin and configure reference dropdown entries.
2. Create an organization and its department/designation (plus contractor for contract employees).
3. Create a customer role using `POST /api/Role/CreateRole`, for example:

```json
{"name":"Department Manager","tenantId":1,"landingPageId":1,"status":1,"permissionIds":[3342]}
```

4. Create an employee using the existing API or `POST /api/ContractEmployee/Create`.
5. Activate that employee through `POST /api/User/{employeeId}/ActivateSystemUser`:

```json
{
  "username":"department.manager",
  "password":"choose-a-strong-password",
  "basicRoleId":1,
  "additionalRoleIds":[],
  "scopes":[{"organizationUnitId":1,"includeDescendants":true}]
}
```

Use the actual generated IDs. Passwords require 12–72 characters and at most 72 UTF-8 bytes. They are stored only as BCrypt hashes and never returned.

| Endpoint | Purpose |
| --- | --- |
| `GET /api/Auth/Me` | Current user, roles/account details, permissions, and effective organization scope |
| `GET /api/Permission/GetAll` | Predefined module/action catalog and selection flags |
| `PUT /api/Role/{id}` | Super Admin edits a customer role |
| `PUT /api/SystemUser/{id}/Roles` | Super Admin sets basic/additional roles |
| `PUT /api/SystemUser/{id}/Scope` | Super Admin replaces explicit organization scopes |
| `PUT /api/SystemUser/{id}/Enabled` | Super Admin enables/disables an account |
| `POST /api/ContractEmployee/Create` | Create an employee of type contract |
| `POST /api/ContractEmployee/GetList` | Scoped contract-employee list |
| `PUT /api/ContractEmployee/{id}` | Edit a contract employee |
| `POST /api/Contractor/Create` | Create a contractor company |
| `POST /api/Contractor/GetList` | Scoped contractor list |
| `PUT /api/Contractor/{id}` | Edit a contractor |

The existing 19 endpoint paths remain. PUT endpoints are also available at `/api/{resource}/{id}` for OrganizationUnit, User, Location, OperationActivity, ObservationType, Equipment, Department, and Designation. They accept the corresponding DTO and enforce its permission and organization scope. Full replacement of nested lists removes omitted children.

## Verification

`mvn verify` exercises the API contracts and RBAC isolation against migrated H2. Set `MYSQL_MIGRATION_TEST=true` for actual MySQL migration and persistence checks. `python scripts/smoke_apis.py --base-url http://127.0.0.1:18080` executes real HTTP requests and writes complete response reports, omitting authentication headers and redacting passwords. Test records are named with `EHS-SMOKE-` and retained for inspection.
