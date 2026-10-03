# Built-in Admin role

Admin can perform all implemented business actions, manage users and roles, configure lookup data, and create any number of child organizations. Organizational data and account management stay within the Admin's assigned tenant and organization tree. Lookup catalogs are shared reference data.

| Capability | Super Admin | Admin |
| --- | --- | --- |
| Create a root organization (`parentId` omitted/null/0) | Yes | No |
| Create children and grandchildren | Yes | Within assigned tree |
| Edit organization details | Yes | Within assigned tree |
| Promote a child to a root | Yes | No |
| Manage masters, employees, contractors and temporary users | Yes | Within assigned tree |
| Create/edit custom roles | Yes | Within own tenant |
| Activate accounts, assign roles/scope, enable/disable users | Yes | Within assigned tree |
| Assign Admin to another account | Yes | Within assigned tree |
| Create, assign or edit Super Admin through APIs | No; backend identity is configured separately | No |

The Admin role is seeded by Liquibase migration 007. Its database ID is generated; discover it using the role API instead of hardcoding a number. `builtInAdmin: true` identifies this protected role. Its global definition has tenant ID 0, but every assigned account retains its own tenant and organizational scope. Existing ordinary roles named Admin are not promoted automatically.

## Assign the first Admin

Authenticate as Super Admin using HTTP Basic (default `ehs-api` / `ehs-api-local`).

1. Call `GET http://localhost:8080/api/Role/GetAllRoles`. Find the entry with `builtInAdmin: true` and record its `id` as `adminRoleId`.
2. Create an employee in the intended parent organization, or use an existing employee.
3. Ensure the employee has a nonblank, unique email value for the login identifier and activation-message recipient, then activate the employee. Email syntax is not validated; a value that is not deliverable may be rejected by the SMTP provider. Replace the angle-bracket placeholders below with actual values:

```http
POST http://localhost:8080/api/User/<employeeId>/ActivateSystemUser
Content-Type: application/json

{
  "basicRoleId": <adminRoleId>,
  "additionalRoleIds": [],
  "scopes": [
    { "organizationUnitId": <parentOrganizationId>, "includeDescendants": true }
  ]
}
```

For an existing account, use `PUT /api/SystemUser/<accountId>/Roles` with `{"basicRoleId":<adminRoleId>,"additionalRoleIds":[]}`, and configure its scope with `PUT /api/SystemUser/<accountId>/Scope`.

Admin automatically receives all business actions and descendant access under its scope anchors, including children created later. This descendant behavior applies even when an existing scope has `includeDescendants: false`. Ordinary users continue to obey that flag. Additional employee memberships do not establish account scope.

## Create child organizations as Admin

The Admin first receives an emailed temporary password and must complete [first-login reset](account-onboarding.md). Then authenticate using the employee email and new password:

```http
POST http://localhost:8080/api/OrganizationUnit
Content-Type: application/json

{
  "name": "Factory A Workshop",
  "parentId": <parentOrganizationId>,
  "status": 1,
  "line1": "Workshop Road",
  "country": <countryId>,
  "state": <stateId>,
  "city": <cityId>,
  "languageID": <languageId>,
  "timeZoneID": <timeZoneId>,
  "keyContactName": "Workshop Manager",
  "phoneNumber": "9000000000",
  "emailAddress": "workshop@example.com"
}
```

The organization inherits the Admin's tenant and must have a parent in that Admin's allowed tree. Find the created ID using `GET /api/OrganizationUnit/GetAllOrganizations`; use that ID as `parentId` to create a grandchild. There is no configured child-count limit.

`GET /api/Auth/Me` returns `admin: true`, `superAdmin: false`, effective permissions, and accessible organizational IDs. Removing the Admin role takes effect on the next authenticated request. Custom role names or submitted role flags cannot grant the built-in authority.

Activation accepts roles/scopes only; the employee email becomes the username and a server-generated temporary password is emailed. Built-in Admin accounts also require first-login reset before administrative APIs are available.
