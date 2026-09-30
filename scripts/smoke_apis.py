"""Exercise every documented API over real HTTP. Leaves named test records for inspection."""
import argparse
import base64
import datetime
import json
import os
from pathlib import Path
import time
import urllib.error
import urllib.request
import uuid


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--base-url', default='http://127.0.0.1:18080')
    parser.add_argument('--output', default='reports/api-smoke-results.json')
    args = parser.parse_args()
    credentials = '{}:{}'.format(os.getenv('API_USERNAME', 'ehs-api'), os.getenv('API_PASSWORD', 'ehs-api-local'))
    authorization = 'Basic ' + base64.b64encode(credentials.encode()).decode()
    prefix = 'EHS-SMOKE-' + uuid.uuid4().hex[:8]
    calls = []
    checks = []

    def check(label, condition):
        checks.append({'check': label, 'passed': bool(condition)})

    def call(label, path, body=None, expected=200, auth='valid', envelope=True, method=None):
        method = method or ('GET' if body is None else 'POST')
        headers = {'Content-Type': 'application/json'}
        if auth == 'valid':
            headers['Authorization'] = authorization
        elif auth == 'wrong':
            headers['Authorization'] = 'Basic ' + base64.b64encode(b'ehs-api:incorrect').decode()
        elif isinstance(auth, tuple):
            headers['Authorization'] = 'Basic ' + base64.b64encode((':'.join(auth)).encode()).decode()
        data = None if body is None else json.dumps(body, ensure_ascii=False).encode('utf-8')
        request = urllib.request.Request(args.base_url + path, data=data, headers=headers, method=method)
        start = time.monotonic()
        try:
            response = urllib.request.urlopen(request, timeout=30)
        except urllib.error.HTTPError as error:
            response = error
        status = response.status
        text = response.read().decode('utf-8')
        try:
            result = json.loads(text)
        except json.JSONDecodeError:
            result = text
        passed = status == expected
        if envelope:
            passed = passed and isinstance(result, dict) and result.get('statusCode') == expected and 'results' in result
        safe_body = json.loads(json.dumps(body)) if body is not None else None
        if isinstance(safe_body, dict) and 'password' in safe_body:
            safe_body['password'] = '[REDACTED]'
        calls.append({'case': label, 'method': method, 'path': path, 'request': safe_body,
                      'expectedStatus': expected, 'httpStatus': status, 'passed': passed,
                      'elapsedMs': round((time.monotonic() - start) * 1000), 'response': result})
        if isinstance(result, dict):
            return result.get('results') if envelope else result
        return result

    def listing(label, path, organization, **extra):
        body = {'sorting': 'createdDate', 'sortingType': 'desc', 'filter': '', 'filters': [],
                'maxResultCount': 10, 'skipCount': 0, 'multiSortMeta': [],
                'businessUnitIds': str(organization), 'userId': 0, 'isExportToExcel': False}
        body.update(extra)
        return call(label, path, body)

    try:
        # Explicit synthetic reference data for this smoke run; no assumed global catalog.
        for kind, identifier, item in [
            ('COUNTRY', 900001, {'name': 'Smoke Test Country', 'code': '99', 'currency': 'TST', 'symbol': 'T'}),
            ('STATE', 900002, {'name': 'Smoke Test State', 'countryId': 900001}),
            ('CITY', 900003, {'name': 'Smoke Test City', 'countryId': 900001, 'stateId': 900002}),
            ('LANGUAGE', 900004, {'name': 'Smoke Test Language', 'countryId': 900001}),
            ('TIME_ZONE', 900005, {'name': 'Smoke UTC', 'countryId': 900001, 'zoneId': 'UTC'})]:
            call('Configure ' + kind, f'/api/Lookup/{kind}/{identifier}', item, method='PUT')
        call('Country-filtered state dropdown', '/api/Lookup/STATE?countryId=900001')
        call('Create organization', '/api/OrganizationUnit', {
            'id': 0, 'name': prefix, 'description': 'Live HTTP verification', 'tenantId': 1,
            'parentId': None, 'status': 1, 'line1': 'Test address', 'line2': '', 'city': 900003,
            'state': 900002, 'country': 900001, 'countryCode': '99', 'currency': 'TST', 'symbol': 'T',
            'timeZone': 'UTC+05:30', 'keyContactName': 'Smoke Test', 'phoneNumber': '9000000000',
            'emailAddress': 'smoke@example.com', 'isAnonymous': True, 'isObservationProofRequired': True,
            'languageID': 900004, 'timeZoneID': 900005, 'attachments': None, 'shifts': []})
        tree = call('List organizations', '/api/OrganizationUnit/GetAllOrganizations')
        organization = next(item['id'] for item in tree if item['name'] == prefix)
        department = call('Create department', '/api/Department/Create', {
            'id': 0, 'name': prefix + '-Department', 'businessUnitId': organization, 'description': 'Operations',
            'status': 1, 'translations': [{'languageId': 1, 'name': 'Operations', 'description': 'Operations'}]})['id']
        designation = call('Create designation', '/api/Designation/Create', {
            'id': 0, 'name': prefix + '-Designation', 'businessUnitId': organization, 'description': 'Manager',
            'status': 1, 'translations': [{'languageId': 1, 'name': 'Manager', 'description': 'Manager'}]})['id']
        role = call('Create role', '/api/Role/CreateRole', {
            'id': 0, 'name': prefix + '-Role', 'roleDescription': 'Verification observer', 'displayName': 'Observer',
            'landingPageId': 1, 'roleType': 'Observer', 'status': 1,
            'permissionLookupHierarchyDto': [{'id': 1024, 'name': 'Observation', 'displayName': 'Observation',
                'isGranted': False, 'parentId': 1024, 'status': 1, 'children': [
                    {'id': 2031, 'name': 'CreateObservation', 'displayName': 'Create Observation',
                     'isGranted': True, 'status': 1, 'parentId': 1024, 'children': []}]}]})['id']
        employee = call('Create employee', '/api/User/CreateEmployee', {
            'id': 0, 'firstName': prefix, 'middleName': 'API', 'lastName': 'Tester', 'dateOfBirth': '1994-04-03',
            'age': '32', 'alias': 'smoke', 'country': 900001, 'contractorId': 0, 'dateOfJoining': None,
            'department': department, 'designation': designation, 'emailAddress': 'smoke@example.com',
            'hasAccess': True, 'isMobileUser': False, 'gender': 1, 'phoneNumber': '9000000000', 'status': 1,
            'userNumber': prefix, 'userType': 1, 'organizationUnitId': organization,
            'profilePictureId': 0, 'languageID': 900004, 'userRoleIds': [role]})
        account = call('Activate employee as system user', f'/api/User/{employee}/ActivateSystemUser', {
            'username': prefix.lower(), 'password': 'Smoke-test-password-123', 'basicRoleId': role,
            'scopes': [{'organizationUnitId': organization, 'includeDescendants': True}]})
        location_body = {'name': prefix + '-Location', 'locationSupervisor': '', 'locationDescription': 'Live verification',
            'organizationUnitId': organization, 'status': 1, 'createdBy': employee, 'supervisorIds': [employee],
            'subLocationNames': 'Zone A, Zone B', 'newSublocations': [
                {'name': 'Zone A', 'description': 'First area', 'status': 1},
                {'name': 'Zone B', 'description': '', 'status': 1}], 'subLocations': [], 'subLocationIds': [],
            'translations': [{'languageId': 1, 'name': 'Factory', 'description': 'Live verification',
                              'subLocations': [{'name': 'Zone A', 'description': 'First area'}]}]}
        location = call('Create location', '/api/Location/add', location_body)
        activity = call('Create operation activity', '/api/OperationActivity', {
            'id': 0, 'activityName': prefix + '-Activity', 'businessUnitId': organization, 'categoryId': 1,
            'description': 'Forklift operations', 'status': 1, 'createdBy': employee,
            'translations': [{'languageId': 1, 'activityName': 'Forklift operations', 'description': 'Routine'}]})['id']
        observation = call('Create observation type', '/api/ObservationType/Create', {
            'observationCategoryId': 4, 'typeDescription': prefix + '-Observation', 'businessUnitId': organization,
            'observationSubTypes': [{'subTypeDescription': 'PPE', 'status': 1}, {'subTypeDescription': 'Housekeeping', 'status': 1}],
            'enableSvt': False, 'status': 1, 'modifiedBy': employee,
            'translations': [{'languageId': 1, 'typeDescription': 'Safe behavior', 'businessUnitId': organization,
                'observationSubTypes': [{'subTypeDescription': 'PPE'}, {'subTypeDescription': 'Housekeeping'}]}]})
        equipment_body = {'id': 0, 'equipmentCategoryId': 3, 'equipmentTypeId': 18, 'organizationUnitId': organization,
            'uid': prefix + '-Equipment', 'manufacturer': 'Acme', 'modelNumber': 'M-1', 'serialNumber': prefix,
            'yearofManufacture': '2022', 'status': 1, 'createdBy': employee}
        call('Create equipment', '/api/Equipment/Add', equipment_body)

        users = listing('List users', '/api/User/GetUsers', organization)
        employees = listing('List employees', '/api/User/GetAllEmployees', organization)
        locations = listing('List locations', '/api/Location/GetAllLocations', organization)
        roles = call('List roles', '/api/Role/GetAllRoles')
        activities = listing('List operation activities', '/api/OperationActivity/list', organization)
        observations = listing('List observation types', '/api/ObservationType/GetAll', organization, sorting='id')
        equipment = listing('List equipment', '/api/Equipment/List', organization)
        designations = listing('List designations', '/api/Designation/GetList', organization)
        departments = listing('List departments', '/api/Department/GetList', organization)
        for label, result in [('users', users), ('employees', employees), ('locations', locations),
                              ('activities', activities), ('observations', observations), ('equipment', equipment),
                              ('designations', designations), ('departments', departments)]:
            check(label + ' persisted and business-unit filtered', result['totalCount'] == 1 and len(result['items']) == 1)
        check('employee derived names', employees['items'][0]['departmentName'] == prefix + '-Department')
        check('employee password suppressed', employees['items'][0]['password'] is None)
        check('location generated child IDs', all(c['id'] > 0 for c in locations['items'][0]['subLocations']))
        check('location translation linked to child', locations['items'][0]['translations'][0]['subLocations'][0]['subLocationId'] > 0)
        check('observation translation linked to child', observations['items'][0]['translations'][0]['observationSubTypes'][0]['observationSubTypeId'] > 0)
        check('role grant persisted', next(r for r in roles if r['id'] == role)['permissions'][0]['isGranted'])
        filtered = listing('Filtering and descending sort', '/api/Department/GetList', organization,
            filters=[{'field': 'name', 'matchMode': 'contains', 'value': prefix}], multiSortMeta=[{'field': 'id', 'order': -1}])
        check('filtered totalCount', filtered['totalCount'] == 1)
        paged = listing('Offset pagination', '/api/Department/GetList', organization, skipCount=1, maxResultCount=1)
        check('offset preserves totalCount', paged['totalCount'] == 1 and paged['items'] == [])
        call('Missing credentials', '/api/Role/GetAllRoles', expected=401, auth='none')
        call('Wrong credentials', '/api/Role/GetAllRoles', expected=401, auth='wrong')
        call('Missing required fields', '/api/Department/Create', {}, expected=400)
        call('Unknown organization', '/api/Department/Create', {'name': 'Missing organization', 'businessUnitId': 9223372036854775807}, expected=404)
        call('Duplicate equipment', '/api/Equipment/Add', equipment_body, expected=409)
        call('Null filter entry', '/api/Department/GetList', {'filters': [None]}, expected=400)
        call('Null sort entry', '/api/Department/GetList', {'multiSortMeta': [None]}, expected=400)
        invalid_location = dict(location_body, name=prefix + '-Invalid', newSublocations=[],
            translations=[{'languageId': 1, 'name': 'Invalid', 'subLocations': [None]}])
        call('Null nested location translation', '/api/Location/add', invalid_location, expected=400)
        call('Oversized sublocation', '/api/Location/add', dict(location_body, name=prefix + '-Oversized',
            newSublocations=[{'name': 'X' * 256, 'status': 1}], translations=[]), expected=400)
        call('Oversized sublocation description', '/api/Location/add', dict(location_body, name=prefix + '-Oversized',
            newSublocations=[{'name': 'Child', 'description': 'X' * 256, 'status': 1}], translations=[]), expected=400)
        call('Oversized observation subtype', '/api/ObservationType/Create', {
            'observationCategoryId': 4, 'typeDescription': prefix + '-Oversized', 'businessUnitId': organization,
            'observationSubTypes': [{'subTypeDescription': 'X' * 256, 'status': 1}]}, expected=400)
        call('Null permission child', '/api/Role/CreateRole', {
            'name': prefix + '-Invalid-role', 'permissionLookupHierarchyDto': [{'id': 1, 'children': [None]}]}, expected=400)
        call('Null nested observation translation', '/api/ObservationType/Create', {
            'observationCategoryId': 4, 'typeDescription': prefix + '-Invalid', 'businessUnitId': organization,
            'observationSubTypes': [{'subTypeDescription': 'PPE', 'status': 1}],
            'translations': [{'languageId': 1, 'observationSubTypes': [None]}]}, expected=400)
        final_locations = listing('Failed location creates leave no records', '/api/Location/GetAllLocations', organization)
        check('invalid location rollback', final_locations['totalCount'] == 1)
        final_observations = listing('Failed observation create leaves no records', '/api/ObservationType/GetAll', organization)
        check('invalid observation rollback', final_observations['totalCount'] == 1)

        # Exercise new workbook/RBAC functionality and every edit endpoint.
        unit_dto = next(item for item in tree if item['id'] == organization)
        # aliases in request DTOs accept the canonical lowercase-Id response fields.
        unit_dto['shifts'] = [{'name': 'Night', 'startTime': '22:00', 'endTime': '06:00'}]
        call('Edit organization and shifts', f'/api/OrganizationUnit/{organization}', unit_dto, method='PUT')
        for title, resource, records in [
            ('department', 'Department', departments), ('designation', 'Designation', designations),
            ('equipment', 'Equipment', equipment), ('operation activity', 'OperationActivity', activities),
            ('location', 'Location', locations), ('observation type', 'ObservationType', observations),
            ('employee', 'User', employees)]:
            dto = records['items'][0]
            call('Edit ' + title, f'/api/{resource}/{dto["id"]}', dto, method='PUT')
        role_dto = next(item for item in roles if item['id'] == role)
        call('Edit customer role', f'/api/Role/{role}', role_dto, method='PUT')
        permissions = call('Predefined permission catalog', '/api/Permission/GetAll')
        source_role = json.loads(Path('src/test/resources/document-role-payload.json').read_text())
        source_role.update(name=prefix + '-SourceRole', status=1)
        call('Complete document role permission tree', '/api/Role/CreateRole', source_role)
        check('source permission catalog includes documented actions',
              {'SubmitIncident', 'ViewObservation', 'MyTasks', 'ViewShiftInspectionChecklist'} <= {p['name'] for p in permissions})
        call('Super Admin current access', '/api/Auth/Me')
        department_role = call('Create setup manager role', '/api/Role/CreateRole', {
            'name': prefix + '-SetupRole', 'landingPageId': 1, 'permissionIds': [3342], 'status': 1})['id']
        call('Assign additional role', f'/api/SystemUser/{account["id"]}/Roles',
             {'basicRoleId': role, 'additionalRoleIds': [department_role]}, method='PUT')
        call('Update separate BU scope', f'/api/SystemUser/{account["id"]}/Scope',
             {'scopes': [{'organizationUnitId': organization, 'includeDescendants': False}]}, method='PUT')
        normal_auth = (prefix.lower(), 'Smoke-test-password-123')
        me = call('Normal user current access', '/api/Auth/Me', auth=normal_auth)
        check('normal user scope is separate from roles', me['organizationUnitIds'] == [organization])
        call('Normal user permitted master list', '/api/Department/GetList', {}, auth=normal_auth)
        call('Normal user denied ungranted master', '/api/Equipment/List', {}, auth=normal_auth, expected=403)
        call('Normal user denied role assignment', f'/api/SystemUser/{account["id"]}/Roles',
             {'basicRoleId': department_role}, auth=normal_auth, expected=403, method='PUT')
        call('Normal user denied outside scope', '/api/Department/GetList', {'businessUnitIds': '9223372036854775807'}, auth=normal_auth, expected=403)
        call('Deactivate account', f'/api/SystemUser/{account["id"]}/Enabled', {'enabled': False}, method='PUT')
        call('Disabled user rejected', '/api/Auth/Me', auth=normal_auth, expected=401)
        call('Reactivate account', f'/api/SystemUser/{account["id"]}/Enabled', {'enabled': True}, method='PUT')
        contractor = call('Create contractor company', '/api/Contractor/Create', {'name': prefix + '-Contractor', 'businessUnitId': organization, 'status': 1})['id']
        call('List contractors', '/api/Contractor/GetList', {'businessUnitIds': str(organization)})
        call('Edit contractor', f'/api/Contractor/{contractor}', {'name': prefix + '-Contractor', 'businessUnitId': organization, 'status': 1}, method='PUT')
        contract_body = {'firstName': prefix, 'lastName': 'Contractor', 'gender': 1, 'userNumber': prefix + '-Contract',
                         'organizationUnitId': organization, 'designation': designation, 'contractorId': contractor,
                         'languageID': 900004, 'status': 1}
        contract_employee = call('Create contract employee', '/api/ContractEmployee/Create', contract_body)
        call('List contract employees', '/api/ContractEmployee/GetList', {'businessUnitIds': str(organization)})
        call('Edit contract employee', f'/api/ContractEmployee/{contract_employee}', contract_body, method='PUT')
        # Exact routes and fields added in all master api with response.docx.
        contractor_body = {'contractorName': prefix + '-FullContractor', 'contractorCode': prefix + '-C',
            'businessUnitId': organization, 'status': 1, 'servicesOffered': 'Safety audit',
            'addressLine1': 'Test road', 'addressLine2': 'Suite 2', 'countryId': 900001,
            'stateId': 900002, 'cityId': 900003, 'postalCode': '123456',
            'primaryContactPersonName': 'Test Contact', 'primaryContactDesignation': 'Manager',
            'phoneNumber': '9000000000', 'email': 'contractor@example.com', 'website': 'example.com', 'linkedIn': ''}
        call('Document contractor create', '/api/Contractor/Add', contractor_body)
        full_contractors = listing('Document contractor list', '/api/Contractor/GetAllContractors', organization,
                                  filter=prefix + '-FullContractor', sorting='contractorName')
        full_contractor = full_contractors['items'][0]
        check('contractor full fields persisted', full_contractor['contractorCode'] == prefix + '-C'
              and full_contractor['city'] == 'Smoke Test City' and full_contractor['website'] == 'example.com')
        call('Document contract employee create', '/api/User/CreateEmployee',
             dict(contract_body, userType=2, department=0, contractorId=full_contractor['id'], userNumber=prefix + '-Contract2'))
        contract_list = listing('Document contract employee list', '/api/User/GetAllContractEmployees', organization)
        check('contract employee endpoint filters worker type', contract_list['totalCount'] == 2
              and all(item['userType'] == 2 for item in contract_list['items']))
        administrator = call('Create dynamic administrator role', '/api/Role/CreateRole', {
            'name': prefix + '-Admin', 'landingPageId': 1, 'permissionIds': [3302, 3272, 3274, 3357], 'status': 1})['id']
        call('Grant administration to system user', f'/api/SystemUser/{account["id"]}/Roles',
             {'basicRoleId': administrator}, method='PUT')
        dynamic = call('Admin creates role from catalog', '/api/Role/CreateRole', {
            'name': prefix + '-Dynamic', 'landingPageId': 1, 'permissionIds': [3342], 'status': 1}, auth=normal_auth)['id']
        call('Admin edits role', f'/api/Role/{dynamic}', {
            'name': prefix + '-DynamicEdited', 'landingPageId': 1, 'permissionIds': [3342], 'status': 1}, auth=normal_auth, method='PUT')
        call('Admin assigns role to user', f'/api/SystemUser/{account["id"]}/Roles',
             {'basicRoleId': administrator, 'additionalRoleIds': [dynamic]}, auth=normal_auth, method='PUT')
        temporary_body = {'id': 0, 'roleId': dynamic, 'firstName': prefix, 'middleName': '', 'lastName': 'Auditor',
            'emailAddress': 'temporary@example.com', 'phoneNumber': '9000000000', 'gender': 1, 'status': 1,
            'organizationUnitId': organization, 'alias': '', 'country': 900001, 'companyName': 'Audit Company',
            'hasAccess': True, 'age': 0, 'organizationUnitListIds': [],
            'externalDetails': {'companyName': 'Audit Company', 'designation': 'Auditor', 'status': 1}}
        temporary = call('Document temporary user create', '/api/User/CreateOrUpdateExternalCollabarator', temporary_body, auth=normal_auth)
        temporary_list = listing('Document temporary user list', '/api/User/GetAllExternalCollabarator', organization)
        check('temporary user fields persist', temporary_list['items'][0]['externalDetails']['designation'] == 'Auditor'
              and temporary_list['items'][0]['roleId'] == dynamic and temporary_list['items'][0]['password'] is None)
        call('Update temporary user', '/api/User/CreateOrUpdateExternalCollabarator',
             dict(temporary_body, id=temporary, lastName='Updated'), auth=normal_auth)
        call('Invalid temporary user role rejected', '/api/User/CreateOrUpdateExternalCollabarator',
             dict(temporary_body, roleId=9223372036854775807), expected=400)
        call('Invalid contractor geography rejected', '/api/Contractor/Add', dict(contractor_body, cityId=9223372036854775807), expected=400)
        built_in_admin = next(r['id'] for r in roles if r.get('builtInAdmin'))
        call('Assign built-in Admin', f'/api/SystemUser/{account["id"]}/Roles',
             {'basicRoleId': built_in_admin}, method='PUT')
        admin_me = call('Built-in Admin current access', '/api/Auth/Me', auth=normal_auth)
        check('Admin is not Super Admin', admin_me['admin'] and not admin_me['superAdmin'])
        child_body = dict(unit_dto, id=0, name=prefix + '-Child', parentId=organization, children=[])
        call('Admin creates child organization', '/api/OrganizationUnit', child_body, auth=normal_auth)
        admin_tree = call('Admin lists its tree', '/api/OrganizationUnit/GetAllOrganizations', auth=normal_auth)
        child = next(c['id'] for r in admin_tree if r['id'] == organization for c in r['children'] if c['name'] == prefix + '-Child')
        call('Admin creates grandchild organization', '/api/OrganizationUnit',
             dict(child_body, name=prefix + '-Grandchild', parentId=child), auth=normal_auth)
        call('Admin denied root creation', '/api/OrganizationUnit', dict(child_body, parentId=None), auth=normal_auth, expected=403)
        call('Admin denied zero parent', '/api/OrganizationUnit', dict(child_body, parentId=0), auth=normal_auth, expected=403)
        call('Admin denied root promotion', f'/api/OrganizationUnit/{child}',
             dict(child_body, id=child, parentId=None), auth=normal_auth, method='PUT', expected=403)
        call('Admin creates department in new child', '/api/Department/Create',
             {'name': 'Child Safety', 'businessUnitId': child, 'status': 1}, auth=normal_auth)
        admin_created_account = call('Admin activates employee account', f'/api/User/{contract_employee}/ActivateSystemUser',
             {'username': prefix.lower() + '-delegated', 'password': 'Smoke-test-password-123', 'basicRoleId': built_in_admin,
              'scopes': [{'organizationUnitId': organization, 'includeDescendants': True}]}, auth=normal_auth)
        call('Admin changes account scope', f'/api/SystemUser/{admin_created_account["id"]}/Scope',
             {'scopes': [{'organizationUnitId': child, 'includeDescendants': True}]}, method='PUT', auth=normal_auth)
        call('Admin disables account', f'/api/SystemUser/{admin_created_account["id"]}/Enabled',
             {'enabled': False}, method='PUT', auth=normal_auth)
        call('Admin cannot create Super Admin role', '/api/Role/CreateRole',
             {'name': 'Super Admin', 'landingPageId': 1, 'permissionIds': [3342]}, auth=normal_auth, expected=400)
        api = call('OpenAPI endpoint coverage', '/v3/api-docs', envelope=False)
        covered = {(c['method'].lower(), c['path']) for c in calls if c['httpStatus'] == 200}
        documented = {(method, path) for path, ops in api['paths'].items() for method in ops if method in ['get', 'post', 'put', 'patch', 'delete']}
        import re
        missing = [(method, path) for method, path in documented if not any(
            actual_method == method and re.fullmatch(re.sub(r'\{[^}]+\}', r'[^/]+', path), actual_path.split('?')[0])
            for actual_method, actual_path in covered)]
        check('all documented endpoints called successfully', not missing)
        if missing:
            checks.append({'check': 'Uncovered endpoints', 'passed': False, 'error': str(missing)})
    except Exception as error:
        checks.append({'check': 'Harness completed', 'passed': False, 'error': str(error)})
    failed = sum(not c['passed'] for c in calls) + sum(not c['passed'] for c in checks)
    report = {'generatedAt': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'baseUrl': args.base_url,
              'database': 'ehs_db (MySQL)', 'recordPrefix': prefix,
              'note': 'Test records are retained. Authentication headers and passwords are omitted.',
              'requestCount': len(calls), 'failedChecks': failed, 'calls': calls, 'checks': checks}
    destination = Path(args.output)
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    summary = ['# Live API verification', '', f'- Database: `{report["database"]}`', f'- Server: `{args.base_url}`',
        f'- Test record prefix: `{prefix}`', f'- Requests: {len(calls)}; failed checks: {failed}',
        '', f'Full request and response bodies: [{destination.name}]({destination.name})', '',
        '| Case | Method | Endpoint | Expected | Actual | Result |', '| --- | --- | --- | --- | --- | --- |']
    for c in calls:
        summary.append(f'| {c["case"]} | {c["method"]} | `{c["path"]}` | {c["expectedStatus"]} | {c["httpStatus"]} | {"PASS" if c["passed"] else "FAIL"} |')
    summary.extend(['', '## Response assertions', ''])
    summary.extend(f'- {"PASS" if c["passed"] else "FAIL"}: {c["check"]}' + (': ' + c['error'] if 'error' in c else '') for c in checks)
    destination.with_suffix('.md').write_text('\n'.join(summary) + '\n', encoding='utf-8')
    print(json.dumps({'requests': len(calls), 'failedChecks': failed, 'recordPrefix': prefix, 'report': str(destination)}))
    for c in calls:
        if not c['passed']:
            print(f'FAIL: {c["case"]}: HTTP {c["httpStatus"]}, expected {c["expectedStatus"]}; response={c["response"]}')
    for c in checks:
        if not c['passed']:
            print(c)
    return 1 if failed else 0


if __name__ == '__main__':
    raise SystemExit(main())
