# API URLs and sample payloads

Run the ordered [IntelliJ HTTP collection](master-api-tests.http) to create fresh sample data and capture IDs automatically.
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
  "name": "EHS-SMOKE-4b09952e",
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
  "name": "EHS-SMOKE-4b09952e-Department",
  "businessUnitId": 12,
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
  "name": "EHS-SMOKE-4b09952e-Designation",
  "businessUnitId": 12,
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
  "name": "EHS-SMOKE-4b09952e-Role",
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
  "firstName": "EHS-SMOKE-4b09952e",
  "middleName": "API",
  "lastName": "Tester",
  "dateOfBirth": "1994-04-03",
  "age": "32",
  "alias": "smoke",
  "country": 900001,
  "contractorId": 0,
  "dateOfJoining": null,
  "department": 6,
  "designation": 6,
  "emailAddress": "smoke@example.com",
  "hasAccess": true,
  "isMobileUser": false,
  "gender": 1,
  "phoneNumber": "9000000000",
  "status": 1,
  "userNumber": "EHS-SMOKE-4b09952e",
  "userType": 1,
  "organizationUnitId": 12,
  "profilePictureId": 0,
  "languageID": 900004,
  "userRoleIds": [
    15
  ]
}
```

Expected `results`: `12`.

## Activate employee as system user

`POST http://127.0.0.1:8080/api/User/12/ActivateSystemUser`

```json
{
  "username": "ehs-smoke-4b09952e",
  "password": "Example-password-123",
  "basicRoleId": 15,
  "scopes": [
    {
      "organizationUnitId": 12,
      "includeDescendants": true
    }
  ]
}
```

## Create location

`POST http://127.0.0.1:8080/api/Location/add`

```json
{
  "name": "EHS-SMOKE-4b09952e-Location",
  "locationSupervisor": "",
  "locationDescription": "Live verification",
  "organizationUnitId": 12,
  "status": 1,
  "createdBy": 12,
  "supervisorIds": [
    12
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

Expected `results`: `14`.

## Create operation activity

`POST http://127.0.0.1:8080/api/OperationActivity`

```json
{
  "id": 0,
  "activityName": "EHS-SMOKE-4b09952e-Activity",
  "businessUnitId": 12,
  "categoryId": 1,
  "description": "Forklift operations",
  "status": 1,
  "createdBy": 12,
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
  "typeDescription": "EHS-SMOKE-4b09952e-Observation",
  "businessUnitId": 12,
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
  "modifiedBy": 12,
  "translations": [
    {
      "languageId": 1,
      "typeDescription": "Safe behavior",
      "businessUnitId": 12,
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
  "organizationUnitId": 12,
  "uid": "EHS-SMOKE-4b09952e-Equipment",
  "manufacturer": "Acme",
  "modelNumber": "M-1",
  "serialNumber": "EHS-SMOKE-4b09952e",
  "yearofManufacture": "2022",
  "status": 1,
  "createdBy": 12
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
  "userId": 0,
  "isExportToExcel": false
}
```

## Edit organization and shifts

`PUT http://127.0.0.1:8080/api/OrganizationUnit/12`

```json
{
  "id": 12,
  "createdBy": null,
  "createdDate": "2026-09-30T07:41:05.393948",
  "modifiedBy": null,
  "modifiedDate": null,
  "layoutImageUrl": null,
  "latitude": null,
  "longitude": null,
  "name": "EHS-SMOKE-4b09952e",
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

`PUT http://127.0.0.1:8080/api/Department/6`

```json
{
  "id": 6,
  "createdBy": null,
  "createdDate": "2026-09-30T07:41:05.751689",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-4b09952e-Department",
  "description": "Operations",
  "status": 1,
  "businessUnitId": 12,
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

`PUT http://127.0.0.1:8080/api/Designation/6`

```json
{
  "id": 6,
  "createdBy": null,
  "createdDate": "2026-09-30T07:41:05.926596",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-4b09952e-Designation",
  "description": "Manager",
  "status": 1,
  "businessUnitId": 12,
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

`PUT http://127.0.0.1:8080/api/Equipment/10`

```json
{
  "id": 10,
  "createdBy": 12,
  "createdDate": "2026-09-30T07:41:07.774209",
  "modifiedBy": null,
  "modifiedDate": null,
  "equipmentCategoryId": 3,
  "equipmentTypeId": 18,
  "organizationUnitId": 12,
  "uid": "EHS-SMOKE-4b09952e-Equipment",
  "manufacturer": "Acme",
  "modelNumber": "M-1",
  "serialNumber": "EHS-SMOKE-4b09952e",
  "yearofManufacture": "2022",
  "status": 1,
  "equipmentCategoryName": null,
  "equipmentTypeName": null,
  "organizationUnitNames": "EHS-SMOKE-4b09952e",
  "active": "Active"
}
```

## Edit operation activity

`PUT http://127.0.0.1:8080/api/OperationActivity/6`

```json
{
  "id": 6,
  "createdBy": 12,
  "createdDate": "2026-09-30T07:41:07.36487",
  "modifiedBy": null,
  "modifiedDate": null,
  "activityName": "EHS-SMOKE-4b09952e-Activity",
  "businessUnitId": 12,
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
  "businessUnitName": "EHS-SMOKE-4b09952e"
}
```

## Edit location

`PUT http://127.0.0.1:8080/api/Location/14`

```json
{
  "id": 14,
  "createdBy": 12,
  "createdDate": "2026-09-30T07:41:07.217544",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-4b09952e-Location",
  "locationDescription": "Live verification",
  "status": 1,
  "organizationUnitId": 12,
  "supervisorIds": [
    12
  ],
  "translations": [
    {
      "languageId": 1,
      "name": "Factory",
      "description": "Live verification",
      "subLocations": [
        {
          "subLocationId": 17,
          "name": "Zone A",
          "description": "First area"
        }
      ]
    }
  ],
  "statusName": "Active",
  "supervisorNames": "EHS-SMOKE-4b09952e API Tester (EHS-SMOKE-4b09952e)",
  "subLocationNames": "Zone A, Zone B",
  "organizationUnitName": "EHS-SMOKE-4b09952e",
  "createByName": "EHS-SMOKE-4b09952e API Tester (EHS-SMOKE-4b09952e)",
  "locationSupervisor": null,
  "subLocationIds": [],
  "newSublocations": null,
  "subLocations": [
    {
      "id": 17,
      "name": "Zone A",
      "description": "First area",
      "status": 1
    },
    {
      "id": 18,
      "name": "Zone B",
      "description": "",
      "status": 1
    }
  ]
}
```

Expected `results`: `14`.

## Edit observation type

`PUT http://127.0.0.1:8080/api/ObservationType/7`

```json
{
  "id": 7,
  "createdBy": null,
  "createdDate": "2026-09-30T07:41:07.55639",
  "modifiedBy": 12,
  "modifiedDate": null,
  "observationCategoryId": 4,
  "typeDescription": "EHS-SMOKE-4b09952e-Observation",
  "enableSvt": false,
  "status": 1,
  "businessUnitId": 12,
  "isDefault": null,
  "translations": [
    {
      "languageId": 1,
      "typeDescription": "Safe behavior",
      "businessUnitId": 12,
      "observationSubTypes": [
        {
          "languageId": 1,
          "observationSubTypeId": 12,
          "subTypeDescription": "PPE"
        },
        {
          "languageId": 1,
          "observationSubTypeId": 13,
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
      "id": 12,
      "observationTypeId": 7,
      "subTypeDescription": "PPE",
      "status": 1
    },
    {
      "id": 13,
      "observationTypeId": 7,
      "subTypeDescription": "Housekeeping",
      "status": 1
    }
  ]
}
```

## Edit employee

`PUT http://127.0.0.1:8080/api/User/12`

```json
{
  "id": 12,
  "createdBy": null,
  "createdDate": "2026-09-30T07:41:06.348633",
  "modifiedBy": null,
  "modifiedDate": null,
  "systemAccountId": 5,
  "basicRoleId": 15,
  "additionalRoleIds": [],
  "systemUsername": "ehs-smoke-4b09952e",
  "systemAccountEnabled": true,
  "countryCode": "99",
  "firstName": "EHS-SMOKE-4b09952e",
  "middleName": "API",
  "lastName": "Tester",
  "emailAddress": "smoke@example.com",
  "phoneNumber": "9000000000",
  "profilePictureId": 0,
  "gender": 1,
  "department": 6,
  "dateOfJoining": null,
  "contractorId": 0,
  "accessFailedCount": null,
  "designation": 6,
  "userType": 1,
  "status": 1,
  "userNumber": "EHS-SMOKE-4b09952e",
  "tenantID": 1,
  "languageID": 900004,
  "organizationUnitId": 12,
  "hasAccess": true,
  "isMobileUser": false,
  "alias": "smoke",
  "dateOfBirth": "1994-04-03",
  "age": 32,
  "country": 900001,
  "isSubscribed": false,
  "userRoleIds": [
    15
  ],
  "organizationUnitIds": [
    12
  ],
  "organizationUnitIdsMapped": [
    12
  ],
  "organizationUnitListIds": [
    12
  ],
  "uploadedFiles": null,
  "password": "Example-password-123",
  "departmentName": "EHS-SMOKE-4b09952e-Department",
  "designationName": "EHS-SMOKE-4b09952e-Designation",
  "contractorName": null,
  "organizationUnitNames": "EHS-SMOKE-4b09952e",
  "workerTypeName": "Employee",
  "userRoleNames": "EHS-SMOKE-4b09952e-Role",
  "genderText": null,
  "contractorNameText": null,
  "fullName": "EHS-SMOKE-4b09952e API Tester (EHS-SMOKE-4b09952e)",
  "organizationUnitName": "EHS-SMOKE-4b09952e",
  "countryName": "Smoke Test Country",
  "profilePictureUrl": null
}
```

Expected `results`: `12`.

## Edit customer role

`PUT http://127.0.0.1:8080/api/Role/15`

```json
{
  "id": 15,
  "createdBy": null,
  "createdDate": "2026-09-30T07:41:06.05186",
  "modifiedBy": null,
  "modifiedDate": null,
  "tenantId": 1,
  "systemRole": false,
  "builtInAdmin": false,
  "permissionIds": [
    2031
  ],
  "name": "EHS-SMOKE-4b09952e-Role",
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

`PUT http://127.0.0.1:8080/api/SystemUser/5/Roles`

```json
{
  "basicRoleId": 15,
  "additionalRoleIds": [
    17
  ]
}
```

## Update separate BU scope

`PUT http://127.0.0.1:8080/api/SystemUser/5/Scope`

```json
{
  "scopes": [
    {
      "organizationUnitId": 12,
      "includeDescendants": false
    }
  ]
}
```

## Deactivate account

`PUT http://127.0.0.1:8080/api/SystemUser/5/Enabled`

```json
{
  "enabled": false
}
```

## Create contractor company

`POST http://127.0.0.1:8080/api/Contractor/Create`

```json
{
  "name": "EHS-SMOKE-4b09952e-Contractor",
  "businessUnitId": 12,
  "status": 1
}
```

## List contractors

`POST http://127.0.0.1:8080/api/Contractor/GetList`

```json
{
  "businessUnitIds": "12"
}
```

## Edit contractor

`PUT http://127.0.0.1:8080/api/Contractor/7`

```json
{
  "name": "EHS-SMOKE-4b09952e-Contractor",
  "businessUnitId": 12,
  "status": 1
}
```

## Create contract employee

`POST http://127.0.0.1:8080/api/ContractEmployee/Create`

```json
{
  "firstName": "EHS-SMOKE-4b09952e",
  "lastName": "Contractor",
  "gender": 1,
  "userNumber": "EHS-SMOKE-4b09952e-Contract",
  "organizationUnitId": 12,
  "designation": 6,
  "contractorId": 7,
  "languageID": 900004,
  "status": 1
}
```

Expected `results`: `13`.

## List contract employees

`POST http://127.0.0.1:8080/api/ContractEmployee/GetList`

```json
{
  "businessUnitIds": "12"
}
```

## Edit contract employee

`PUT http://127.0.0.1:8080/api/ContractEmployee/13`

```json
{
  "firstName": "EHS-SMOKE-4b09952e",
  "lastName": "Contractor",
  "gender": 1,
  "userNumber": "EHS-SMOKE-4b09952e-Contract",
  "organizationUnitId": 12,
  "designation": 6,
  "contractorId": 7,
  "languageID": 900004,
  "status": 1
}
```

Expected `results`: `13`.

## Document contractor create

`POST http://127.0.0.1:8080/api/Contractor/Add`

```json
{
  "contractorName": "EHS-SMOKE-4b09952e-FullContractor",
  "contractorCode": "EHS-SMOKE-4b09952e-C",
  "businessUnitId": 12,
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
  "filter": "EHS-SMOKE-4b09952e-FullContractor",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "12",
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
  "businessUnitIds": "12",
  "userId": 0,
  "isExportToExcel": false
}
```

## Document temporary user create

`POST http://127.0.0.1:8080/api/User/CreateOrUpdateExternalCollabarator`

```json
{
  "id": 0,
  "roleId": 19,
  "firstName": "EHS-SMOKE-4b09952e",
  "middleName": "",
  "lastName": "Auditor",
  "emailAddress": "temporary@example.com",
  "phoneNumber": "9000000000",
  "gender": 1,
  "status": 1,
  "organizationUnitId": 12,
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

Expected `results`: `4`.

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
  "businessUnitIds": "12",
  "userId": 0,
  "isExportToExcel": false
}
```
