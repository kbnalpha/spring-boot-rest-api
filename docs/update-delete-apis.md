# Update and delete master records

Individual executable deletion requests are in [delete-api-tests.http](delete-api-tests.http). Supply disposable test IDs; do not run the entire file against real employee data.

Use HTTP Basic authentication and `Content-Type: application/json` for updates. All existing PUT endpoints accept the complete editable DTO, as used for creation; they are not PATCH operations. Omitted values can reset fields or collections. Read the existing record and preserve values you want to keep.

Admin and Super Admin can update all supported masters. Admin remains restricted to its tenant and assigned organization tree and cannot create a root organization or grant Super Admin. Ordinary users retain their assigned update permissions. All DELETE endpoints below require Admin or Super Admin; business permissions alone do not authorize deletion.

| Resource | Update | Delete |
| --- | --- | --- |
| Organization | `PUT /api/OrganizationUnit/{id}` | `DELETE /api/OrganizationUnit/{id}` |
| Department | `PUT /api/Department/{id}` | `DELETE /api/Department/{id}` |
| Designation | `PUT /api/Designation/{id}` | `DELETE /api/Designation/{id}` |
| Location and sublocations | `PUT /api/Location/{id}` | `DELETE /api/Location/{id}` |
| Operation activity | `PUT /api/OperationActivity/{id}` | `DELETE /api/OperationActivity/{id}` |
| Observation type and subtypes | `PUT /api/ObservationType/{id}` | `DELETE /api/ObservationType/{id}` |
| Equipment | `PUT /api/Equipment/{id}` | `DELETE /api/Equipment/{id}` |
| Contractor | `PUT /api/Contractor/{id}` | `DELETE /api/Contractor/{id}` |
| Employee | `PUT /api/User/{id}` | `DELETE /api/User/{id}` |
| Contract employee | `PUT /api/ContractEmployee/{id}` | `DELETE /api/ContractEmployee/{id}` |
| External collaborator | `PUT /api/User/ExternalCollaborator/{id}` | `DELETE /api/User/ExternalCollaborator/{id}` |
| Role | `PUT /api/Role/{id}` | `DELETE /api/Role/{id}` |
| Lookup | `PUT /api/Lookup/{kind}/{id}` (upsert) | `DELETE /api/Lookup/{kind}/{id}` |
| System account | `PUT /api/SystemUser/{id}/Roles`, `/Scope`, `/Enabled` | `DELETE /api/SystemUser/{id}` |

The existing external collaborator create-or-update POST remains supported. DELETE `/api/ExternalCollaborator/{id}` is also available. Sublocations and observation subtypes are edited through their parent PUT payloads; include all children to retain, with existing IDs. Omitting a child removes it. Deleting a parent location/type removes its owned children.

DELETE requires no body and returns HTTP 200 with `{"statusCode":200,"message":"Successful","results":"Deleted successfully."}`. Missing records return 404; scope/privilege failures return 403. Referenced records return 409 and the transaction rolls back. Built-in Admin and Super Admin roles cannot be deleted.

Organizations with dependent records cannot be deleted. Departments/designations referenced by employees, contractors with employees, assigned roles, lookup values in use, and employees referenced as supervisors or by system accounts are protected. Delete a system account first if you intend to subsequently delete its employee. Deleting a system account removes its role/scope mappings, clears employee login access and role metadata, and retains the employee; future authentication fails. For reversible suspension, prefer `/Enabled` with `{"enabled":false}`.

Example department update (replace IDs with your own):

```http
PUT {{baseUrl}}/api/Department/{{departmentId}}
Authorization: Basic {{username}} {{password}}
Content-Type: application/json

{
  "name": "Site Operations",
  "description": "Updated operations department",
  "businessUnitId": {{organizationId}},
  "status": 1,
  "translations": []
}
```

Example deletion of an unused test department:

```http
DELETE {{baseUrl}}/api/Department/{{unusedDepartmentId}}
Authorization: Basic {{username}} {{password}}
```

Deletion permanently removes data. The application does not recursively delete organization trees or automatically reassign dependents. No live database records were deleted while implementing these APIs; integration tests use isolated H2 data.
