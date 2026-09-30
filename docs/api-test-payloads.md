# API URLs and sample payloads

Use the ordered [IntelliJ HTTP collection](master-api-tests.http). Configure SMTP, run through activation, read the emailed temporary password, set the temporaryPassword variable, then continue. IDs are captured automatically.
See [email onboarding and first-login APIs](account-onboarding.md). Activation accepts role/scope only; username is the employee email and the server generates the temporary password. Pending users cannot call business APIs.
For the built-in Admin role, account activation, and child-organization restrictions, see [Admin setup](admin-role.md). The HTTP collection also assigns Admin and creates a child using that account.
The examples below are successful requests from the MySQL verification run. Replace IDs with your own records when testing separately; use unique employee numbers and equipment UIDs for new records.
Base URL: `http://127.0.0.1:8080`. HTTP Basic: `ehs-api` / `ehs-api-local` (or your configured API credentials).
Responses use `{statusCode,message,results}`. All operations below expect HTTP 200. Account password examples are synthetic.

## Configure COUNTRY

`PUT http://127.0.0.1:8080/api/Lookup/COUNTRY/900001`

```json
{
  "name": "Smoke Test Country",
  "code": "99",
  "currency": "TST",
  "symbol": "T"
}
```

## Configure STATE

`PUT http://127.0.0.1:8080/api/Lookup/STATE/900002`

```json
{
  "name": "Smoke Test State",
  "countryId": 900001
}
```

## Configure CITY

`PUT http://127.0.0.1:8080/api/Lookup/CITY/900003`

```json
{
  "name": "Smoke Test City",
  "countryId": 900001,
  "stateId": 900002
}
```

## Configure LANGUAGE

`PUT http://127.0.0.1:8080/api/Lookup/LANGUAGE/900004`

```json
{
  "name": "Smoke Test Language",
  "countryId": 900001
}
```

## Configure TIME_ZONE

`PUT http://127.0.0.1:8080/api/Lookup/TIME_ZONE/900005`

```json
{
  "name": "Smoke UTC",
  "countryId": 900001,
  "zoneId": "UTC"
}
```

## Country-filtered state dropdown

`GET http://127.0.0.1:8080/api/Lookup/STATE?countryId=900001`

No payload.

## Create organization

`POST http://127.0.0.1:8080/api/OrganizationUnit`

```json
{
  "id": 0,
  "name": "EHS-SMOKE-a13cc7f0",
  "description": "Live HTTP verification",
  "tenantId": 1,
  "parentId": null,
  "status": 1,
  "line1": "Test address",
  "line2": "",
  "city": 900003,
  "state": 900002,
  "country": 900001,
  "countryCode": "99",
  "currency": "TST",
  "symbol": "T",
  "timeZone": "UTC+05:30",
  "keyContactName": "Smoke Test",
  "phoneNumber": "9000000000",
  "emailAddress": "smoke@example.com",
  "isAnonymous": true,
  "isObservationProofRequired": true,
  "languageID": 900004,
  "timeZoneID": 900005,
  "attachments": null,
  "shifts": []
}
```

Expected `results`: `"Organization Unit created successfully."`.

## List organizations

`GET http://127.0.0.1:8080/api/OrganizationUnit/GetAllOrganizations`

No payload.

## Create department

`POST http://127.0.0.1:8080/api/Department/Create`

```json
{
  "id": 0,
  "name": "EHS-SMOKE-a13cc7f0-Department",
  "businessUnitId": 19,
  "description": "Operations",
  "status": 1,
  "translations": [
    {
      "languageId": 1,
      "name": "Operations",
      "description": "Operations"
    }
  ]
}
```

## Create designation

`POST http://127.0.0.1:8080/api/Designation/Create`

```json
{
  "id": 0,
  "name": "EHS-SMOKE-a13cc7f0-Designation",
  "businessUnitId": 19,
  "description": "Manager",
  "status": 1,
  "translations": [
    {
      "languageId": 1,
      "name": "Manager",
      "description": "Manager"
    }
  ]
}
```

## Create role

`POST http://127.0.0.1:8080/api/Role/CreateRole`

```json
{
  "id": 0,
  "name": "EHS-SMOKE-a13cc7f0-Role",
  "roleDescription": "Verification observer",
  "displayName": "Observer",
  "landingPageId": 1,
  "roleType": "Observer",
  "status": 1,
  "permissionLookupHierarchyDto": [
    {
      "id": 1024,
      "name": "Observation",
      "displayName": "Observation",
      "isGranted": false,
      "parentId": 1024,
      "status": 1,
      "children": [
        {
          "id": 2031,
          "name": "CreateObservation",
          "displayName": "Create Observation",
          "isGranted": true,
          "status": 1,
          "parentId": 1024,
          "children": []
        }
      ]
    }
  ]
}
```

## Create employee

`POST http://127.0.0.1:8080/api/User/CreateEmployee`

```json
{
  "id": 0,
  "firstName": "EHS-SMOKE-a13cc7f0",
  "middleName": "API",
  "lastName": "Tester",
  "dateOfBirth": "1994-04-03",
  "age": "32",
  "alias": "smoke",
  "country": 900001,
  "contractorId": 0,
  "dateOfJoining": null,
  "department": 9,
  "designation": 8,
  "emailAddress": "ehs-smoke-a13cc7f0@example.com",
  "hasAccess": true,
  "isMobileUser": false,
  "gender": 1,
  "phoneNumber": "9000000000",
  "status": 1,
  "userNumber": "EHS-SMOKE-a13cc7f0",
  "userType": 1,
  "organizationUnitId": 19,
  "profilePictureId": 0,
  "languageID": 900004,
  "userRoleIds": [
    21
  ]
}
```

Expected `results`: `17`.

## Activate employee as system user

`POST http://127.0.0.1:8080/api/User/17/ActivateSystemUser`

```json
{
  "basicRoleId": 21,
  "scopes": [
    {
      "organizationUnitId": 19,
      "includeDescendants": true
    }
  ]
}
```

## First login requires reset

`POST http://127.0.0.1:8080/api/Auth/Login`

```json
{
  "username": "ehs-smoke-a13cc7f0@example.com",
  "password": "TEMPORARY_PASSWORD_FROM_EMAIL"
}
```

## Set first permanent password

`POST http://127.0.0.1:8080/api/Auth/FirstLoginPasswordReset`

```json
{
  "currentPassword": "TEMPORARY_PASSWORD_FROM_EMAIL",
  "newPassword": "Example-password-123",
  "confirmPassword": "Example-password-123"
}
```

## Create location

`POST http://127.0.0.1:8080/api/Location/add`

```json
{
  "name": "EHS-SMOKE-a13cc7f0-Location",
  "locationSupervisor": "",
  "locationDescription": "Live verification",
  "organizationUnitId": 19,
  "status": 1,
  "createdBy": 17,
  "supervisorIds": [
    17
  ],
  "subLocationNames": "Zone A, Zone B",
  "newSublocations": [
    {
      "name": "Zone A",
      "description": "First area",
      "status": 1
    },
    {
      "name": "Zone B",
      "description": "",
      "status": 1
    }
  ],
  "subLocations": [],
  "subLocationIds": [],
  "translations": [
    {
      "languageId": 1,
      "name": "Factory",
      "description": "Live verification",
      "subLocations": [
        {
          "name": "Zone A",
          "description": "First area"
        }
      ]
    }
  ]
}
```

Expected `results`: `18`.

## Create operation activity

`POST http://127.0.0.1:8080/api/OperationActivity`

```json
{
  "id": 0,
  "activityName": "EHS-SMOKE-a13cc7f0-Activity",
  "businessUnitId": 19,
  "categoryId": 1,
  "description": "Forklift operations",
  "status": 1,
  "createdBy": 17,
  "translations": [
    {
      "languageId": 1,
      "activityName": "Forklift operations",
      "description": "Routine"
    }
  ]
}
```

## Create observation type

`POST http://127.0.0.1:8080/api/ObservationType/Create`

```json
{
  "observationCategoryId": 4,
  "typeDescription": "EHS-SMOKE-a13cc7f0-Observation",
  "businessUnitId": 19,
  "observationSubTypes": [
    {
      "subTypeDescription": "PPE",
      "status": 1
    },
    {
      "subTypeDescription": "Housekeeping",
      "status": 1
    }
  ],
  "enableSvt": false,
  "status": 1,
  "modifiedBy": 17,
  "translations": [
    {
      "languageId": 1,
      "typeDescription": "Safe behavior",
      "businessUnitId": 19,
      "observationSubTypes": [
        {
          "subTypeDescription": "PPE"
        },
        {
          "subTypeDescription": "Housekeeping"
        }
      ]
    }
  ]
}
```

## Create equipment

`POST http://127.0.0.1:8080/api/Equipment/Add`

```json
{
  "id": 0,
  "equipmentCategoryId": 3,
  "equipmentTypeId": 18,
  "organizationUnitId": 19,
  "uid": "EHS-SMOKE-a13cc7f0-Equipment",
  "manufacturer": "Acme",
  "modelNumber": "M-1",
  "serialNumber": "EHS-SMOKE-a13cc7f0",
  "yearofManufacture": "2022",
  "status": 1,
  "createdBy": 17
}
```

Expected `results`: `"Equipment added successfully."`.

## List users

`POST http://127.0.0.1:8080/api/User/GetUsers`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List employees

`POST http://127.0.0.1:8080/api/User/GetAllEmployees`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List locations

`POST http://127.0.0.1:8080/api/Location/GetAllLocations`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List roles

`GET http://127.0.0.1:8080/api/Role/GetAllRoles`

No payload.

## List operation activities

`POST http://127.0.0.1:8080/api/OperationActivity/list`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List observation types

`POST http://127.0.0.1:8080/api/ObservationType/GetAll`

```json
{
  "sorting": "id",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List equipment

`POST http://127.0.0.1:8080/api/Equipment/List`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List designations

`POST http://127.0.0.1:8080/api/Designation/GetList`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## List departments

`POST http://127.0.0.1:8080/api/Department/GetList`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## Edit organization and shifts

`PUT http://127.0.0.1:8080/api/OrganizationUnit/19`

```json
{
  "id": 19,
  "createdBy": null,
  "createdDate": "2026-09-30T08:52:21.257112",
  "modifiedBy": null,
  "modifiedDate": null,
  "layoutImageUrl": null,
  "latitude": null,
  "longitude": null,
  "name": "EHS-SMOKE-a13cc7f0",
  "description": "Live HTTP verification",
  "tenantId": 1,
  "parentId": null,
  "status": 1,
  "currency": "TST",
  "line1": "Test address",
  "line2": "",
  "city": 900003,
  "state": 900002,
  "country": 900001,
  "countryCode": "99",
  "symbol": "T",
  "timeZone": "UTC",
  "timeZoneId": 900005,
  "languageId": 900004,
  "keyContactName": "Smoke Test",
  "phoneNumber": "9000000000",
  "emailAddress": "smoke@example.com",
  "isAnonymous": true,
  "isObservationProofRequired": true,
  "attachments": null,
  "shifts": [
    {
      "name": "Night",
      "startTime": "22:00",
      "endTime": "06:00"
    }
  ],
  "buImage": null,
  "cityName": "Smoke Test City",
  "stateName": "Smoke Test State",
  "countryName": "Smoke Test Country",
  "timeZoneName": "Smoke UTC",
  "languageName": "Smoke Test Language",
  "shiftMaster": null,
  "children": []
}
```

## Edit department

`PUT http://127.0.0.1:8080/api/Department/9`

```json
{
  "id": 9,
  "createdBy": null,
  "createdDate": "2026-09-30T08:52:21.597769",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-a13cc7f0-Department",
  "description": "Operations",
  "status": 1,
  "businessUnitId": 19,
  "translations": [
    {
      "languageId": 1,
      "name": "Operations",
      "description": "Operations"
    }
  ],
  "statusDisplay": "Active"
}
```

## Edit designation

`PUT http://127.0.0.1:8080/api/Designation/8`

```json
{
  "id": 8,
  "createdBy": null,
  "createdDate": "2026-09-30T08:52:21.80557",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-a13cc7f0-Designation",
  "description": "Manager",
  "status": 1,
  "businessUnitId": 19,
  "translations": [
    {
      "languageId": 1,
      "name": "Manager",
      "description": "Manager"
    }
  ],
  "statusDisplay": "Active"
}
```

## Edit equipment

`PUT http://127.0.0.1:8080/api/Equipment/13`

```json
{
  "id": 13,
  "createdBy": 17,
  "createdDate": "2026-09-30T08:52:24.691531",
  "modifiedBy": null,
  "modifiedDate": null,
  "equipmentCategoryId": 3,
  "equipmentTypeId": 18,
  "organizationUnitId": 19,
  "uid": "EHS-SMOKE-a13cc7f0-Equipment",
  "manufacturer": "Acme",
  "modelNumber": "M-1",
  "serialNumber": "EHS-SMOKE-a13cc7f0",
  "yearofManufacture": "2022",
  "status": 1,
  "equipmentCategoryName": null,
  "equipmentTypeName": null,
  "organizationUnitNames": "EHS-SMOKE-a13cc7f0",
  "active": "Active"
}
```

## Edit operation activity

`PUT http://127.0.0.1:8080/api/OperationActivity/8`

```json
{
  "id": 8,
  "createdBy": 17,
  "createdDate": "2026-09-30T08:52:24.317628",
  "modifiedBy": null,
  "modifiedDate": null,
  "activityName": "EHS-SMOKE-a13cc7f0-Activity",
  "businessUnitId": 19,
  "categoryId": 1,
  "description": "Forklift operations",
  "status": 1,
  "translations": [
    {
      "languageId": 1,
      "activityName": "Forklift operations",
      "description": "Routine"
    }
  ],
  "category": "Routine",
  "businessUnitName": "EHS-SMOKE-a13cc7f0"
}
```

## Edit location

`PUT http://127.0.0.1:8080/api/Location/18`

```json
{
  "id": 18,
  "createdBy": 17,
  "createdDate": "2026-09-30T08:52:24.14739",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-a13cc7f0-Location",
  "locationDescription": "Live verification",
  "status": 1,
  "organizationUnitId": 19,
  "supervisorIds": [
    17
  ],
  "translations": [
    {
      "languageId": 1,
      "name": "Factory",
      "description": "Live verification",
      "subLocations": [
        {
          "subLocationId": 23,
          "name": "Zone A",
          "description": "First area"
        }
      ]
    }
  ],
  "statusName": "Active",
  "supervisorNames": "EHS-SMOKE-a13cc7f0 API Tester (EHS-SMOKE-a13cc7f0)",
  "subLocationNames": "Zone A, Zone B",
  "organizationUnitName": "EHS-SMOKE-a13cc7f0",
  "createByName": "EHS-SMOKE-a13cc7f0 API Tester (EHS-SMOKE-a13cc7f0)",
  "locationSupervisor": null,
  "subLocationIds": [],
  "newSublocations": null,
  "subLocations": [
    {
      "id": 23,
      "name": "Zone A",
      "description": "First area",
      "status": 1
    },
    {
      "id": 24,
      "name": "Zone B",
      "description": "",
      "status": 1
    }
  ]
}
```

Expected `results`: `18`.

## Edit observation type

`PUT http://127.0.0.1:8080/api/ObservationType/9`

```json
{
  "id": 9,
  "createdBy": null,
  "createdDate": "2026-09-30T08:52:24.541852",
  "modifiedBy": 17,
  "modifiedDate": null,
  "observationCategoryId": 4,
  "typeDescription": "EHS-SMOKE-a13cc7f0-Observation",
  "enableSvt": false,
  "status": 1,
  "businessUnitId": 19,
  "isDefault": null,
  "translations": [
    {
      "languageId": 1,
      "typeDescription": "Safe behavior",
      "businessUnitId": 19,
      "observationSubTypes": [
        {
          "languageId": 1,
          "observationSubTypeId": 16,
          "subTypeDescription": "PPE"
        },
        {
          "languageId": 1,
          "observationSubTypeId": 17,
          "subTypeDescription": "Housekeeping"
        }
      ]
    }
  ],
  "observationCategoryName": "Safe Behavior",
  "createdByName": "",
  "observationSubTypeDescriptions": "PPE, Housekeeping",
  "enableSvtDisplay": "false",
  "statusDisplay": "Active",
  "isEditDelete": true,
  "observationSubTypes": [
    {
      "id": 16,
      "observationTypeId": 9,
      "subTypeDescription": "PPE",
      "status": 1
    },
    {
      "id": 17,
      "observationTypeId": 9,
      "subTypeDescription": "Housekeeping",
      "status": 1
    }
  ]
}
```

## Edit employee

`PUT http://127.0.0.1:8080/api/User/17`

```json
{
  "id": 17,
  "createdBy": null,
  "createdDate": "2026-09-30T08:52:22.357314",
  "modifiedBy": null,
  "modifiedDate": null,
  "systemAccountId": 8,
  "basicRoleId": 21,
  "additionalRoleIds": [],
  "systemUsername": "ehs-smoke-a13cc7f0@example.com",
  "systemAccountEnabled": true,
  "countryCode": "99",
  "firstName": "EHS-SMOKE-a13cc7f0",
  "middleName": "API",
  "lastName": "Tester",
  "emailAddress": "ehs-smoke-a13cc7f0@example.com",
  "phoneNumber": "9000000000",
  "profilePictureId": 0,
  "gender": 1,
  "department": 9,
  "dateOfJoining": null,
  "contractorId": 0,
  "accessFailedCount": null,
  "designation": 8,
  "userType": 1,
  "status": 1,
  "userNumber": "EHS-SMOKE-a13cc7f0",
  "tenantID": 1,
  "languageID": 900004,
  "organizationUnitId": 19,
  "hasAccess": true,
  "isMobileUser": false,
  "alias": "smoke",
  "dateOfBirth": "1994-04-03",
  "age": 32,
  "country": 900001,
  "isSubscribed": false,
  "userRoleIds": [
    21
  ],
  "organizationUnitIds": [
    19
  ],
  "organizationUnitIdsMapped": [
    19
  ],
  "organizationUnitListIds": [
    19
  ],
  "uploadedFiles": null,
  "password": "Example-password-123",
  "departmentName": "EHS-SMOKE-a13cc7f0-Department",
  "designationName": "EHS-SMOKE-a13cc7f0-Designation",
  "contractorName": null,
  "organizationUnitNames": "EHS-SMOKE-a13cc7f0",
  "workerTypeName": "Employee",
  "userRoleNames": "EHS-SMOKE-a13cc7f0-Role",
  "genderText": null,
  "contractorNameText": null,
  "fullName": "EHS-SMOKE-a13cc7f0 API Tester (EHS-SMOKE-a13cc7f0)",
  "organizationUnitName": "EHS-SMOKE-a13cc7f0",
  "countryName": "Smoke Test Country",
  "profilePictureUrl": null
}
```

Expected `results`: `17`.

## Edit customer role

`PUT http://127.0.0.1:8080/api/Role/21`

```json
{
  "id": 21,
  "createdBy": null,
  "createdDate": "2026-09-30T08:52:22.077007",
  "modifiedBy": null,
  "modifiedDate": null,
  "tenantId": 1,
  "systemRole": false,
  "builtInAdmin": false,
  "permissionIds": [
    2031
  ],
  "name": "EHS-SMOKE-a13cc7f0-Role",
  "displayName": "Observer",
  "status": 1,
  "roleDescription": "Verification observer",
  "roleType": "Observer",
  "landingPageId": 1,
  "permissions": [
    {
      "id": 2031,
      "name": "CreateObservation",
      "displayName": "Create Observation",
      "isGranted": true,
      "status": 1,
      "parentId": 1024,
      "children": []
    }
  ],
  "roleOrganizationUnits": [],
  "userRoles": [],
  "createBy": null,
  "permissionLookupHierarchyDto": [
    {
      "id": 2031,
      "name": "CreateObservation",
      "displayName": "Create Observation",
      "isGranted": true,
      "status": 1,
      "parentId": 1024,
      "children": []
    }
  ]
}
```

## Predefined permission catalog

`GET http://127.0.0.1:8080/api/Permission/GetAll`

No payload.

## Super Admin current access

`GET http://127.0.0.1:8080/api/Auth/Me`

No payload.

## Assign additional role

`PUT http://127.0.0.1:8080/api/SystemUser/8/Roles`

```json
{
  "basicRoleId": 21,
  "additionalRoleIds": [
    23
  ]
}
```

## Update separate BU scope

`PUT http://127.0.0.1:8080/api/SystemUser/8/Scope`

```json
{
  "scopes": [
    {
      "organizationUnitId": 19,
      "includeDescendants": false
    }
  ]
}
```

## Deactivate account

`PUT http://127.0.0.1:8080/api/SystemUser/8/Enabled`

```json
{
  "enabled": false
}
```

## Create contractor company

`POST http://127.0.0.1:8080/api/Contractor/Create`

```json
{
  "name": "EHS-SMOKE-a13cc7f0-Contractor",
  "businessUnitId": 19,
  "status": 1
}
```

## List contractors

`POST http://127.0.0.1:8080/api/Contractor/GetList`

```json
{
  "businessUnitIds": "19"
}
```

## Edit contractor

`PUT http://127.0.0.1:8080/api/Contractor/10`

```json
{
  "name": "EHS-SMOKE-a13cc7f0-Contractor",
  "businessUnitId": 19,
  "status": 1
}
```

## Create contract employee

`POST http://127.0.0.1:8080/api/ContractEmployee/Create`

```json
{
  "firstName": "EHS-SMOKE-a13cc7f0",
  "lastName": "Contractor",
  "gender": 1,
  "userNumber": "EHS-SMOKE-a13cc7f0-Contract",
  "emailAddress": "ehs-smoke-a13cc7f0-contract@example.com",
  "organizationUnitId": 19,
  "designation": 8,
  "contractorId": 10,
  "languageID": 900004,
  "status": 1
}
```

Expected `results`: `18`.

## List contract employees

`POST http://127.0.0.1:8080/api/ContractEmployee/GetList`

```json
{
  "businessUnitIds": "19"
}
```

## Edit contract employee

`PUT http://127.0.0.1:8080/api/ContractEmployee/18`

```json
{
  "firstName": "EHS-SMOKE-a13cc7f0",
  "lastName": "Contractor",
  "gender": 1,
  "userNumber": "EHS-SMOKE-a13cc7f0-Contract",
  "emailAddress": "ehs-smoke-a13cc7f0-contract@example.com",
  "organizationUnitId": 19,
  "designation": 8,
  "contractorId": 10,
  "languageID": 900004,
  "status": 1
}
```

Expected `results`: `18`.

## Document contractor create

`POST http://127.0.0.1:8080/api/Contractor/Add`

```json
{
  "contractorName": "EHS-SMOKE-a13cc7f0-FullContractor",
  "contractorCode": "EHS-SMOKE-a13cc7f0-C",
  "businessUnitId": 19,
  "status": 1,
  "servicesOffered": "Safety audit",
  "addressLine1": "Test road",
  "addressLine2": "Suite 2",
  "countryId": 900001,
  "stateId": 900002,
  "cityId": 900003,
  "postalCode": "123456",
  "primaryContactPersonName": "Test Contact",
  "primaryContactDesignation": "Manager",
  "phoneNumber": "9000000000",
  "email": "contractor@example.com",
  "website": "example.com",
  "linkedIn": ""
}
```

Expected `results`: `"Contractor added successfully."`.

## Document contractor list

`POST http://127.0.0.1:8080/api/Contractor/GetAllContractors`

```json
{
  "sorting": "contractorName",
  "sortingType": "desc",
  "filter": "EHS-SMOKE-a13cc7f0-FullContractor",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## Document contract employee list

`POST http://127.0.0.1:8080/api/User/GetAllContractEmployees`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## Document temporary user create

`POST http://127.0.0.1:8080/api/User/CreateOrUpdateExternalCollabarator`

```json
{
  "id": 0,
  "roleId": 25,
  "firstName": "EHS-SMOKE-a13cc7f0",
  "middleName": "",
  "lastName": "Auditor",
  "emailAddress": "temporary@example.com",
  "phoneNumber": "9000000000",
  "gender": 1,
  "status": 1,
  "organizationUnitId": 19,
  "alias": "",
  "country": 900001,
  "companyName": "Audit Company",
  "hasAccess": true,
  "age": 0,
  "organizationUnitListIds": [],
  "externalDetails": {
    "companyName": "Audit Company",
    "designation": "Auditor",
    "status": 1
  }
}
```

Expected `results`: `6`.

## Document temporary user list

`POST http://127.0.0.1:8080/api/User/GetAllExternalCollabarator`

```json
{
  "sorting": "createdDate",
  "sortingType": "desc",
  "filter": "",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "19",
  "userId": 0,
  "isExportToExcel": false
}
```

## Admin resends activation email

`POST http://127.0.0.1:8080/api/SystemUser/9/ResendActivation`

No payload.
