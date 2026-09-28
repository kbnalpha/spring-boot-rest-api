# Live API verification

- Database: `ehs_db (MySQL)`
- Server: `http://127.0.0.1:18080`
- Test record prefix: `EHS-SMOKE-11548e2c`
- Requests: 34; failed checks: 5

Full request and response bodies: [api-smoke-before-fixes.json](api-smoke-before-fixes.json)

| Case | Method | Endpoint | Expected | Actual | Result |
| --- | --- | --- | --- | --- | --- |
| Create organization | POST | `/api/OrganizationUnit` | 200 | 200 | PASS |
| List organizations | GET | `/api/OrganizationUnit/GetAllOrganizations` | 200 | 200 | PASS |
| Create department | POST | `/api/Department/Create` | 200 | 200 | PASS |
| Create designation | POST | `/api/Designation/Create` | 200 | 200 | PASS |
| Create role | POST | `/api/Role/CreateRole` | 200 | 200 | PASS |
| Create employee | POST | `/api/User/CreateEmployee` | 200 | 200 | PASS |
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
| Null filter entry | POST | `/api/Department/GetList` | 400 | 500 | FAIL |
| Null sort entry | POST | `/api/Department/GetList` | 400 | 500 | FAIL |
| Null nested location translation | POST | `/api/Location/add` | 400 | 500 | FAIL |
| Oversized sublocation | POST | `/api/Location/add` | 400 | 409 | FAIL |
| Null nested observation translation | POST | `/api/ObservationType/Create` | 400 | 500 | FAIL |
| Failed location creates leave no records | POST | `/api/Location/GetAllLocations` | 200 | 200 | PASS |
| Failed observation create leaves no records | POST | `/api/ObservationType/GetAll` | 200 | 200 | PASS |
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
- PASS: all 19 documented endpoints called successfully
