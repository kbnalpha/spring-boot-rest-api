# Live API verification

- Database: `ehs_db (MySQL)`
- Server: `http://127.0.0.1:18080`
- Test record prefix: `EHS-SMOKE-4b09952e`
- Requests: 100; failed checks: 0

Full request and response bodies: [api-smoke-results.json](api-smoke-results.json)

| Case | Method | Endpoint | Expected | Actual | Result |
| --- | --- | --- | --- | --- | --- |
| Configure COUNTRY | PUT | `/api/Lookup/COUNTRY/900001` | 200 | 200 | PASS |
| Configure STATE | PUT | `/api/Lookup/STATE/900002` | 200 | 200 | PASS |
| Configure CITY | PUT | `/api/Lookup/CITY/900003` | 200 | 200 | PASS |
| Configure LANGUAGE | PUT | `/api/Lookup/LANGUAGE/900004` | 200 | 200 | PASS |
| Configure TIME_ZONE | PUT | `/api/Lookup/TIME_ZONE/900005` | 200 | 200 | PASS |
| Country-filtered state dropdown | GET | `/api/Lookup/STATE?countryId=900001` | 200 | 200 | PASS |
| Create organization | POST | `/api/OrganizationUnit` | 200 | 200 | PASS |
| List organizations | GET | `/api/OrganizationUnit/GetAllOrganizations` | 200 | 200 | PASS |
| Create department | POST | `/api/Department/Create` | 200 | 200 | PASS |
| Create designation | POST | `/api/Designation/Create` | 200 | 200 | PASS |
| Create role | POST | `/api/Role/CreateRole` | 200 | 200 | PASS |
| Create employee | POST | `/api/User/CreateEmployee` | 200 | 200 | PASS |
| Activate employee as system user | POST | `/api/User/12/ActivateSystemUser` | 200 | 200 | PASS |
| Create location | POST | `/api/Location/add` | 200 | 200 | PASS |
| Create operation activity | POST | `/api/OperationActivity` | 200 | 200 | PASS |
| Create observation type | POST | `/api/ObservationType/Create` | 200 | 200 | PASS |
| Create equipment | POST | `/api/Equipment/Add` | 200 | 200 | PASS |
| List users | POST | `/api/User/GetUsers` | 200 | 200 | PASS |
| List employees | POST | `/api/User/GetAllEmployees` | 200 | 200 | PASS |
| List locations | POST | `/api/Location/GetAllLocations` | 200 | 200 | PASS |
| List roles | GET | `/api/Role/GetAllRoles` | 200 | 200 | PASS |
| List operation activities | POST | `/api/OperationActivity/list` | 200 | 200 | PASS |
| List observation types | POST | `/api/ObservationType/GetAll` | 200 | 200 | PASS |
| List equipment | POST | `/api/Equipment/List` | 200 | 200 | PASS |
| List designations | POST | `/api/Designation/GetList` | 200 | 200 | PASS |
| List departments | POST | `/api/Department/GetList` | 200 | 200 | PASS |
| Filtering and descending sort | POST | `/api/Department/GetList` | 200 | 200 | PASS |
| Offset pagination | POST | `/api/Department/GetList` | 200 | 200 | PASS |
| Missing credentials | GET | `/api/Role/GetAllRoles` | 401 | 401 | PASS |
| Wrong credentials | GET | `/api/Role/GetAllRoles` | 401 | 401 | PASS |
| Missing required fields | POST | `/api/Department/Create` | 400 | 400 | PASS |
| Unknown organization | POST | `/api/Department/Create` | 404 | 404 | PASS |
| Duplicate equipment | POST | `/api/Equipment/Add` | 409 | 409 | PASS |
| Null filter entry | POST | `/api/Department/GetList` | 400 | 400 | PASS |
| Null sort entry | POST | `/api/Department/GetList` | 400 | 400 | PASS |
| Null nested location translation | POST | `/api/Location/add` | 400 | 400 | PASS |
| Oversized sublocation | POST | `/api/Location/add` | 400 | 400 | PASS |
| Oversized sublocation description | POST | `/api/Location/add` | 400 | 400 | PASS |
| Oversized observation subtype | POST | `/api/ObservationType/Create` | 400 | 400 | PASS |
| Null permission child | POST | `/api/Role/CreateRole` | 400 | 400 | PASS |
| Null nested observation translation | POST | `/api/ObservationType/Create` | 400 | 400 | PASS |
| Failed location creates leave no records | POST | `/api/Location/GetAllLocations` | 200 | 200 | PASS |
| Failed observation create leaves no records | POST | `/api/ObservationType/GetAll` | 200 | 200 | PASS |
| Edit organization and shifts | PUT | `/api/OrganizationUnit/12` | 200 | 200 | PASS |
| Edit department | PUT | `/api/Department/6` | 200 | 200 | PASS |
| Edit designation | PUT | `/api/Designation/6` | 200 | 200 | PASS |
| Edit equipment | PUT | `/api/Equipment/10` | 200 | 200 | PASS |
| Edit operation activity | PUT | `/api/OperationActivity/6` | 200 | 200 | PASS |
| Edit location | PUT | `/api/Location/14` | 200 | 200 | PASS |
| Edit observation type | PUT | `/api/ObservationType/7` | 200 | 200 | PASS |
| Edit employee | PUT | `/api/User/12` | 200 | 200 | PASS |
| Edit customer role | PUT | `/api/Role/15` | 200 | 200 | PASS |
| Predefined permission catalog | GET | `/api/Permission/GetAll` | 200 | 200 | PASS |
| Complete document role permission tree | POST | `/api/Role/CreateRole` | 200 | 200 | PASS |
| Super Admin current access | GET | `/api/Auth/Me` | 200 | 200 | PASS |
| Create setup manager role | POST | `/api/Role/CreateRole` | 200 | 200 | PASS |
| Assign additional role | PUT | `/api/SystemUser/5/Roles` | 200 | 200 | PASS |
| Update separate BU scope | PUT | `/api/SystemUser/5/Scope` | 200 | 200 | PASS |
| Normal user current access | GET | `/api/Auth/Me` | 200 | 200 | PASS |
| Normal user permitted master list | POST | `/api/Department/GetList` | 200 | 200 | PASS |
| Normal user denied ungranted master | POST | `/api/Equipment/List` | 403 | 403 | PASS |
| Normal user denied role assignment | PUT | `/api/SystemUser/5/Roles` | 403 | 403 | PASS |
| Normal user denied outside scope | POST | `/api/Department/GetList` | 403 | 403 | PASS |
| Deactivate account | PUT | `/api/SystemUser/5/Enabled` | 200 | 200 | PASS |
| Disabled user rejected | GET | `/api/Auth/Me` | 401 | 401 | PASS |
| Reactivate account | PUT | `/api/SystemUser/5/Enabled` | 200 | 200 | PASS |
| Create contractor company | POST | `/api/Contractor/Create` | 200 | 200 | PASS |
| List contractors | POST | `/api/Contractor/GetList` | 200 | 200 | PASS |
| Edit contractor | PUT | `/api/Contractor/7` | 200 | 200 | PASS |
| Create contract employee | POST | `/api/ContractEmployee/Create` | 200 | 200 | PASS |
| List contract employees | POST | `/api/ContractEmployee/GetList` | 200 | 200 | PASS |
| Edit contract employee | PUT | `/api/ContractEmployee/13` | 200 | 200 | PASS |
| Document contractor create | POST | `/api/Contractor/Add` | 200 | 200 | PASS |
| Document contractor list | POST | `/api/Contractor/GetAllContractors` | 200 | 200 | PASS |
| Document contract employee create | POST | `/api/User/CreateEmployee` | 200 | 200 | PASS |
| Document contract employee list | POST | `/api/User/GetAllContractEmployees` | 200 | 200 | PASS |
| Create dynamic administrator role | POST | `/api/Role/CreateRole` | 200 | 200 | PASS |
| Grant administration to system user | PUT | `/api/SystemUser/5/Roles` | 200 | 200 | PASS |
| Admin creates role from catalog | POST | `/api/Role/CreateRole` | 200 | 200 | PASS |
| Admin edits role | PUT | `/api/Role/19` | 200 | 200 | PASS |
| Admin assigns role to user | PUT | `/api/SystemUser/5/Roles` | 200 | 200 | PASS |
| Document temporary user create | POST | `/api/User/CreateOrUpdateExternalCollabarator` | 200 | 200 | PASS |
| Document temporary user list | POST | `/api/User/GetAllExternalCollabarator` | 200 | 200 | PASS |
| Update temporary user | POST | `/api/User/CreateOrUpdateExternalCollabarator` | 200 | 200 | PASS |
| Invalid temporary user role rejected | POST | `/api/User/CreateOrUpdateExternalCollabarator` | 400 | 400 | PASS |
| Invalid contractor geography rejected | POST | `/api/Contractor/Add` | 400 | 400 | PASS |
| Assign built-in Admin | PUT | `/api/SystemUser/5/Roles` | 200 | 200 | PASS |
| Built-in Admin current access | GET | `/api/Auth/Me` | 200 | 200 | PASS |
| Admin creates child organization | POST | `/api/OrganizationUnit` | 200 | 200 | PASS |
| Admin lists its tree | GET | `/api/OrganizationUnit/GetAllOrganizations` | 200 | 200 | PASS |
| Admin creates grandchild organization | POST | `/api/OrganizationUnit` | 200 | 200 | PASS |
| Admin denied root creation | POST | `/api/OrganizationUnit` | 403 | 403 | PASS |
| Admin denied zero parent | POST | `/api/OrganizationUnit` | 403 | 403 | PASS |
| Admin denied root promotion | PUT | `/api/OrganizationUnit/13` | 403 | 403 | PASS |
| Admin creates department in new child | POST | `/api/Department/Create` | 200 | 200 | PASS |
| Admin activates employee account | POST | `/api/User/13/ActivateSystemUser` | 200 | 200 | PASS |
| Admin changes account scope | PUT | `/api/SystemUser/6/Scope` | 200 | 200 | PASS |
| Admin disables account | PUT | `/api/SystemUser/6/Enabled` | 200 | 200 | PASS |
| Admin cannot create Super Admin role | POST | `/api/Role/CreateRole` | 400 | 400 | PASS |
| OpenAPI endpoint coverage | GET | `/v3/api-docs` | 200 | 200 | PASS |

## Response assertions

- PASS: users persisted and business-unit filtered
- PASS: employees persisted and business-unit filtered
- PASS: locations persisted and business-unit filtered
- PASS: activities persisted and business-unit filtered
- PASS: observations persisted and business-unit filtered
- PASS: equipment persisted and business-unit filtered
- PASS: designations persisted and business-unit filtered
- PASS: departments persisted and business-unit filtered
- PASS: employee derived names
- PASS: employee password suppressed
- PASS: location generated child IDs
- PASS: location translation linked to child
- PASS: observation translation linked to child
- PASS: role grant persisted
- PASS: filtered totalCount
- PASS: offset preserves totalCount
- PASS: invalid location rollback
- PASS: invalid observation rollback
- PASS: source permission catalog includes documented actions
- PASS: normal user scope is separate from roles
- PASS: contractor full fields persisted
- PASS: contract employee endpoint filters worker type
- PASS: temporary user fields persist
- PASS: Admin is not Super Admin
- PASS: all documented endpoints called successfully
