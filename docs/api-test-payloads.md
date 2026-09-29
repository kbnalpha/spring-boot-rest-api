# API URLs and sample payloads

Run the ordered [IntelliJ HTTP collection](master-api-tests.http) to create fresh sample data and capture IDs automatically.
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
  "name": "EHS-SMOKE-9d359016",
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
  "name": "EHS-SMOKE-9d359016-Department",
  "businessUnitId": 8,
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
  "name": "EHS-SMOKE-9d359016-Designation",
  "businessUnitId": 8,
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
  "name": "EHS-SMOKE-9d359016-Role",
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
  "firstName": "EHS-SMOKE-9d359016",
  "middleName": "API",
  "lastName": "Tester",
  "dateOfBirth": "1994-04-03",
  "age": "32",
  "alias": "smoke",
  "country": 900001,
  "contractorId": 0,
  "dateOfJoining": null,
  "department": 4,
  "designation": 4,
  "emailAddress": "smoke@example.com",
  "hasAccess": true,
  "isMobileUser": false,
  "gender": 1,
  "phoneNumber": "9000000000",
  "status": 1,
  "userNumber": "EHS-SMOKE-9d359016",
  "userType": 1,
  "organizationUnitId": 8,
  "profilePictureId": 0,
  "languageID": 900004,
  "userRoleIds": [
    8
  ]
}
```

Expected `results`: `7`.

## Activate employee as system user

`POST http://127.0.0.1:8080/api/User/7/ActivateSystemUser`

```json
{
  "username": "ehs-smoke-9d359016",
  "password": "Example-password-123",
  "basicRoleId": 8,
  "scopes": [
    {
      "organizationUnitId": 8,
      "includeDescendants": true
    }
  ]
}
```

## Create location

`POST http://127.0.0.1:8080/api/Location/add`

```json
{
  "name": "EHS-SMOKE-9d359016-Location",
  "locationSupervisor": "",
  "locationDescription": "Live verification",
  "organizationUnitId": 8,
  "status": 1,
  "createdBy": 7,
  "supervisorIds": [
    7
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

Expected `results`: `10`.

## Create operation activity

`POST http://127.0.0.1:8080/api/OperationActivity`

```json
{
  "id": 0,
  "activityName": "EHS-SMOKE-9d359016-Activity",
  "businessUnitId": 8,
  "categoryId": 1,
  "description": "Forklift operations",
  "status": 1,
  "createdBy": 7,
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
  "typeDescription": "EHS-SMOKE-9d359016-Observation",
  "businessUnitId": 8,
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
  "modifiedBy": 7,
  "translations": [
    {
      "languageId": 1,
      "typeDescription": "Safe behavior",
      "businessUnitId": 8,
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
  "organizationUnitId": 8,
  "uid": "EHS-SMOKE-9d359016-Equipment",
  "manufacturer": "Acme",
  "modelNumber": "M-1",
  "serialNumber": "EHS-SMOKE-9d359016",
  "yearofManufacture": "2022",
  "status": 1,
  "createdBy": 7
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
  "userId": 0,
  "isExportToExcel": false
}
```

## Edit organization and shifts

`PUT http://127.0.0.1:8080/api/OrganizationUnit/8`

```json
{
  "id": 8,
  "createdBy": null,
  "createdDate": "2026-09-29T18:35:32.188644",
  "modifiedBy": null,
  "modifiedDate": null,
  "layoutImageUrl": null,
  "latitude": null,
  "longitude": null,
  "name": "EHS-SMOKE-9d359016",
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

`PUT http://127.0.0.1:8080/api/Department/4`

```json
{
  "id": 4,
  "createdBy": null,
  "createdDate": "2026-09-29T18:35:32.438036",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-9d359016-Department",
  "description": "Operations",
  "status": 1,
  "businessUnitId": 8,
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

`PUT http://127.0.0.1:8080/api/Designation/4`

```json
{
  "id": 4,
  "createdBy": null,
  "createdDate": "2026-09-29T18:35:32.571247",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-9d359016-Designation",
  "description": "Manager",
  "status": 1,
  "businessUnitId": 8,
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

`PUT http://127.0.0.1:8080/api/Equipment/7`

```json
{
  "id": 7,
  "createdBy": 7,
  "createdDate": "2026-09-29T18:35:33.757375",
  "modifiedBy": null,
  "modifiedDate": null,
  "equipmentCategoryId": 3,
  "equipmentTypeId": 18,
  "organizationUnitId": 8,
  "uid": "EHS-SMOKE-9d359016-Equipment",
  "manufacturer": "Acme",
  "modelNumber": "M-1",
  "serialNumber": "EHS-SMOKE-9d359016",
  "yearofManufacture": "2022",
  "status": 1,
  "equipmentCategoryName": null,
  "equipmentTypeName": null,
  "organizationUnitNames": "EHS-SMOKE-9d359016",
  "active": "Active"
}
```

## Edit operation activity

`PUT http://127.0.0.1:8080/api/OperationActivity/4`

```json
{
  "id": 4,
  "createdBy": 7,
  "createdDate": "2026-09-29T18:35:33.470494",
  "modifiedBy": null,
  "modifiedDate": null,
  "activityName": "EHS-SMOKE-9d359016-Activity",
  "businessUnitId": 8,
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
  "businessUnitName": "EHS-SMOKE-9d359016"
}
```

## Edit location

`PUT http://127.0.0.1:8080/api/Location/10`

```json
{
  "id": 10,
  "createdBy": 7,
  "createdDate": "2026-09-29T18:35:33.374955",
  "modifiedBy": null,
  "modifiedDate": null,
  "name": "EHS-SMOKE-9d359016-Location",
  "locationDescription": "Live verification",
  "status": 1,
  "organizationUnitId": 8,
  "supervisorIds": [
    7
  ],
  "translations": [
    {
      "languageId": 1,
      "name": "Factory",
      "description": "Live verification",
      "subLocations": [
        {
          "subLocationId": 11,
          "name": "Zone A",
          "description": "First area"
        }
      ]
    }
  ],
  "statusName": "Active",
  "supervisorNames": "EHS-SMOKE-9d359016 API Tester (EHS-SMOKE-9d359016)",
  "subLocationNames": "Zone A, Zone B",
  "organizationUnitName": "EHS-SMOKE-9d359016",
  "createByName": "EHS-SMOKE-9d359016 API Tester (EHS-SMOKE-9d359016)",
  "locationSupervisor": null,
  "subLocationIds": [],
  "newSublocations": null,
  "subLocations": [
    {
      "id": 11,
      "name": "Zone A",
      "description": "First area",
      "status": 1
    },
    {
      "id": 12,
      "name": "Zone B",
      "description": "",
      "status": 1
    }
  ]
}
```

Expected `results`: `10`.

## Edit observation type

`PUT http://127.0.0.1:8080/api/ObservationType/5`

```json
{
  "id": 5,
  "createdBy": null,
  "createdDate": "2026-09-29T18:35:33.615973",
  "modifiedBy": 7,
  "modifiedDate": null,
  "observationCategoryId": 4,
  "typeDescription": "EHS-SMOKE-9d359016-Observation",
  "enableSvt": false,
  "status": 1,
  "businessUnitId": 8,
  "isDefault": null,
  "translations": [
    {
      "languageId": 1,
      "typeDescription": "Safe behavior",
      "businessUnitId": 8,
      "observationSubTypes": [
        {
          "languageId": 1,
          "observationSubTypeId": 8,
          "subTypeDescription": "PPE"
        },
        {
          "languageId": 1,
          "observationSubTypeId": 9,
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
      "id": 8,
      "observationTypeId": 5,
      "subTypeDescription": "PPE",
      "status": 1
    },
    {
      "id": 9,
      "observationTypeId": 5,
      "subTypeDescription": "Housekeeping",
      "status": 1
    }
  ]
}
```

## Edit employee

`PUT http://127.0.0.1:8080/api/User/7`

```json
{
  "id": 7,
  "createdBy": null,
  "createdDate": "2026-09-29T18:35:32.831182",
  "modifiedBy": null,
  "modifiedDate": null,
  "systemAccountId": 3,
  "basicRoleId": 8,
  "additionalRoleIds": [],
  "systemUsername": "ehs-smoke-9d359016",
  "systemAccountEnabled": true,
  "countryCode": "99",
  "firstName": "EHS-SMOKE-9d359016",
  "middleName": "API",
  "lastName": "Tester",
  "emailAddress": "smoke@example.com",
  "phoneNumber": "9000000000",
  "profilePictureId": 0,
  "gender": 1,
  "department": 4,
  "dateOfJoining": null,
  "contractorId": 0,
  "accessFailedCount": null,
  "designation": 4,
  "userType": 1,
  "status": 1,
  "userNumber": "EHS-SMOKE-9d359016",
  "tenantID": 1,
  "languageID": 900004,
  "organizationUnitId": 8,
  "hasAccess": true,
  "isMobileUser": false,
  "alias": "smoke",
  "dateOfBirth": "1994-04-03",
  "age": 32,
  "country": 900001,
  "isSubscribed": false,
  "userRoleIds": [
    8
  ],
  "organizationUnitIds": [
    8
  ],
  "organizationUnitIdsMapped": [
    8
  ],
  "organizationUnitListIds": [
    8
  ],
  "uploadedFiles": null,
  "password": "Example-password-123",
  "departmentName": "EHS-SMOKE-9d359016-Department",
  "designationName": "EHS-SMOKE-9d359016-Designation",
  "contractorName": null,
  "organizationUnitNames": "EHS-SMOKE-9d359016",
  "workerTypeName": "Employee",
  "userRoleNames": "EHS-SMOKE-9d359016-Role",
  "genderText": null,
  "contractorNameText": null,
  "fullName": "EHS-SMOKE-9d359016 API Tester (EHS-SMOKE-9d359016)",
  "organizationUnitName": "EHS-SMOKE-9d359016",
  "countryName": "Smoke Test Country",
  "profilePictureUrl": null
}
```

Expected `results`: `7`.

## Edit customer role

`PUT http://127.0.0.1:8080/api/Role/8`

```json
{
  "id": 8,
  "createdBy": null,
  "createdDate": "2026-09-29T18:35:32.687203",
  "modifiedBy": null,
  "modifiedDate": null,
  "tenantId": 1,
  "systemRole": false,
  "permissionIds": [
    2031
  ],
  "name": "EHS-SMOKE-9d359016-Role",
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

`PUT http://127.0.0.1:8080/api/SystemUser/3/Roles`

```json
{
  "basicRoleId": 8,
  "additionalRoleIds": [
    10
  ]
}
```

## Update separate BU scope

`PUT http://127.0.0.1:8080/api/SystemUser/3/Scope`

```json
{
  "scopes": [
    {
      "organizationUnitId": 8,
      "includeDescendants": false
    }
  ]
}
```

## Deactivate account

`PUT http://127.0.0.1:8080/api/SystemUser/3/Enabled`

```json
{
  "enabled": false
}
```

## Create contractor company

`POST http://127.0.0.1:8080/api/Contractor/Create`

```json
{
  "name": "EHS-SMOKE-9d359016-Contractor",
  "businessUnitId": 8,
  "status": 1
}
```

## List contractors

`POST http://127.0.0.1:8080/api/Contractor/GetList`

```json
{
  "businessUnitIds": "8"
}
```

## Edit contractor

`PUT http://127.0.0.1:8080/api/Contractor/4`

```json
{
  "name": "EHS-SMOKE-9d359016-Contractor",
  "businessUnitId": 8,
  "status": 1
}
```

## Create contract employee

`POST http://127.0.0.1:8080/api/ContractEmployee/Create`

```json
{
  "firstName": "EHS-SMOKE-9d359016",
  "lastName": "Contractor",
  "gender": 1,
  "userNumber": "EHS-SMOKE-9d359016-Contract",
  "organizationUnitId": 8,
  "designation": 4,
  "contractorId": 4,
  "languageID": 900004,
  "status": 1
}
```

Expected `results`: `8`.

## List contract employees

`POST http://127.0.0.1:8080/api/ContractEmployee/GetList`

```json
{
  "businessUnitIds": "8"
}
```

## Edit contract employee

`PUT http://127.0.0.1:8080/api/ContractEmployee/8`

```json
{
  "firstName": "EHS-SMOKE-9d359016",
  "lastName": "Contractor",
  "gender": 1,
  "userNumber": "EHS-SMOKE-9d359016-Contract",
  "organizationUnitId": 8,
  "designation": 4,
  "contractorId": 4,
  "languageID": 900004,
  "status": 1
}
```

Expected `results`: `8`.

## Document contractor create

`POST http://127.0.0.1:8080/api/Contractor/Add`

```json
{
  "contractorName": "EHS-SMOKE-9d359016-FullContractor",
  "contractorCode": "EHS-SMOKE-9d359016-C",
  "businessUnitId": 8,
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
  "filter": "EHS-SMOKE-9d359016-FullContractor",
  "filters": [],
  "maxResultCount": 10,
  "skipCount": 0,
  "multiSortMeta": [],
  "businessUnitIds": "8",
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
  "businessUnitIds": "8",
  "userId": 0,
  "isExportToExcel": false
}
```

## Document temporary user create

`POST http://127.0.0.1:8080/api/User/CreateOrUpdateExternalCollabarator`

```json
{
  "id": 0,
  "roleId": 12,
  "firstName": "EHS-SMOKE-9d359016",
  "middleName": "",
  "lastName": "Auditor",
  "emailAddress": "temporary@example.com",
  "phoneNumber": "9000000000",
  "gender": 1,
  "status": 1,
  "organizationUnitId": 8,
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

Expected `results`: `2`.

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
  "businessUnitIds": "8",
  "userId": 0,
  "isExportToExcel": false
}
```
