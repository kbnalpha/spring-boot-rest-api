"""Build an executable IntelliJ HTTP collection from successful live requests."""
import json
from pathlib import Path

report=json.loads(Path('reports/api-smoke-results.json').read_text(encoding='utf-8'))
calls={c['case']:c for c in report['calls']}
selected=[
    'Configure COUNTRY','Configure STATE','Configure CITY','Configure LANGUAGE','Configure TIME_ZONE',
    'Country-filtered state dropdown','Create organization','List organizations','Create department','Create designation',
    'Create role','Create employee','Activate employee as system user','First login requires reset','Set first permanent password','Login with permanent password','Create location','Create operation activity',
    'Create observation type','Create equipment','List users','List employees','List locations','List roles',
    'List operation activities','List observation types','List equipment','List designations','List departments',
    'Document contractor create','Document contractor list','Document contract employee create','Document contract employee list',
    'Predefined permission catalog','Document temporary user create','Document temporary user list','Update temporary user',
    'Super Admin current access','Assign built-in Admin','Built-in Admin current access','Admin creates child organization',
]
prefix=report['recordPrefix']
identifiers={
    'organizationId':next(x['id'] for x in calls['List organizations']['response']['results'] if x['name']==prefix),
    'departmentId':calls['Create department']['response']['results']['id'],
    'designationId':calls['Create designation']['response']['results']['id'],
    'roleId':calls['Create role']['response']['results']['id'],
    'employeeId':calls['Create employee']['response']['results'],
    'contractorId':calls['Document contractor list']['response']['results']['items'][0]['id'],
    'temporaryId':calls['Document temporary user create']['response']['results'],
    'accountId':calls['Activate employee as system user']['response']['results']['id'],
    'adminRoleId':next(r['id'] for r in calls['List roles']['response']['results'] if r.get('builtInAdmin')),
}
field_vars={'organizationUnitId':'organizationId','businessUnitId':'organizationId','department':'departmentId',
    'designation':'designationId','contractorId':'contractorId','roleId':'roleId','basicRoleId':'roleId',
    'createdBy':'employeeId','modifiedBy':'employeeId'}
list_vars={'userRoleIds':'roleId','supervisorIds':'employeeId','organizationUnitListIds':'organizationId',
    'organizationUnitIds':'organizationId','organizationUnitIdsMapped':'organizationId'}

def transform(value,key=None):
    if isinstance(value,dict):return {k:transform(v,k) for k,v in value.items()}
    if isinstance(value,list):
        if key in list_vars:return ['{{'+list_vars[key]+'}}' for _ in value]
        return [transform(v) for v in value]
    if key in field_vars and isinstance(value,int) and value>0:return '{{'+field_vars[key]+'}}'
    if key=='businessUnitIds':return '{{organizationId}}'
    if key=='password':return 'Example-password-123'
    if isinstance(value,str):return value.replace(prefix,'{{runName}}').replace(prefix.lower(),'{{runName}}')
    return value

lines=['# EHS master API executable examples',
    '# Configure SMTP and start the application. Run through account activation first.',
    '# Read its email, set temporaryPassword below, then continue the remaining requests.',
    '# Requests run in order; response handlers capture generated IDs.',
    '# Synthetic data is retained. Change baseUrl/credentials if configured differently.',
    '@baseUrl = http://127.0.0.1:8080','@username = ehs-api','@password = ehs-api-local',
    '@temporaryPassword = REPLACE_WITH_PASSWORD_FROM_EMAIL','']
capture={'Create department':('departmentId','response.body.results.id'),
    'Create designation':('designationId','response.body.results.id'),'Create role':('roleId','response.body.results.id'),
    'Create employee':('employeeId','response.body.results'),'Activate employee as system user':('accountId','response.body.results.id'),
    'Document temporary user create':('temporaryId','response.body.results')}
for i,label in enumerate(selected):
    c=calls[label];path=c['path']
    if label=='Activate employee as system user':path='/api/User/{{employeeId}}/ActivateSystemUser'
    if label=='Assign built-in Admin':path='/api/SystemUser/{{accountId}}/Roles'
    body=transform(c['request'])
    if label=='Update temporary user':body['id']='{{temporaryId}}'
    # Use the earlier observer role; no dependency on smoke-only admin test roles.
    if label=='Document temporary user create':body['roleId']='{{roleId}}'
    if label=='Assign built-in Admin':body['basicRoleId']='{{adminRoleId}}'
    if label=='Admin creates child organization':body['parentId']='{{organizationId}}'
    if label in ['First login requires reset','Login with permanent password']:
        body={'username':'{{accountEmail}}','password':'{{temporaryPassword}}' if label=='First login requires reset' else 'Example-password-123'}
    if label=='Set first permanent password':
        body={'currentPassword':'{{temporaryPassword}}','newPassword':'Example-password-123','confirmPassword':'Example-password-123'}
    lines.extend(['### '+label])
    if i==0:lines.extend(['< {%','    client.global.set("runName", "EHS-TEST-" + Date.now());','%}'])
    auth='Authorization: Basic {{accountEmail}} Example-password-123' if label in ['Built-in Admin current access','Admin creates child organization'] else 'Authorization: Basic {{username}} {{password}}'
    if label=='Set first permanent password':auth='Authorization: Basic {{accountEmail}} {{temporaryPassword}}'
    lines.append(c['method']+' {{baseUrl}}'+path)
    if label not in ['First login requires reset','Login with permanent password']:lines.append(auth)
    if body is not None:
        text=json.dumps(body,indent=2,ensure_ascii=False)
        # Numeric ID variables must not be quoted; runName/businessUnitIds remain strings.
        for name in identifiers:text=text.replace('"{{'+name+'}}"','{{'+name+'}}')
        # businessUnitIds is defined as a string.
        text=text.replace('"businessUnitIds": {{organizationId}}','"businessUnitIds": "{{organizationId}}"')
        lines.extend(['Content-Type: application/json','',text])
    lines.extend(['','> {%','    client.test("HTTP 200", function () { client.assert(response.status === 200); });'])
    if label=='List organizations':lines.append('    client.global.set("organizationId", response.body.results.find(x => x.name === client.global.get("runName")).id);')
    if label=='Document contractor list':lines.append('    client.global.set("contractorId", response.body.results.items[0].id);')
    if label=='List roles':lines.append('    client.global.set("adminRoleId", response.body.results.find(x => x.builtInAdmin).id);')
    if label=='Activate employee as system user':lines.append('    client.global.set("accountEmail", response.body.results.username);')
    if label in capture:
        name,expr=capture[label];lines.append(f'    client.global.set("{name}", {expr});')
    lines.extend(['%}',''])
Path('docs').mkdir(exist_ok=True)
Path('docs/master-api-tests.http').write_text('\n'.join(lines),encoding='utf-8')

# Every operation (including backward-compatible aliases and updates) has a concrete example.
seen=set();md=['# API URLs and sample payloads','',
    'Use the ordered [IntelliJ HTTP collection](master-api-tests.http). Configure SMTP, run through activation, read the emailed temporary password, set the temporaryPassword variable, then continue. IDs are captured automatically.',
    'See [email onboarding and first-login APIs](account-onboarding.md). Activation accepts role/scope only; username is the employee email and the server generates the temporary password. Pending users cannot call business APIs.',
    'For the built-in Admin role, account activation, and child-organization restrictions, see [Admin setup](admin-role.md). The HTTP collection also assigns Admin and creates a child using that account.',
    'The examples below are successful requests from the MySQL verification run. Replace IDs with your own records when testing separately; use unique employee numbers and equipment UIDs for new records.',
    'Base URL: `http://127.0.0.1:8080`. HTTP Basic: `ehs-api` / `ehs-api-local` (or your configured API credentials).',
    'Responses use `{statusCode,message,results}`. All operations below expect HTTP 200. Account password examples are synthetic.', '']
for c in report['calls']:
    if not c['passed'] or c['httpStatus']!=200 or c['path']=='/v3/api-docs':continue
    import re
    canonical=re.sub(r'/\d+(?=/|$)','/{id}',c['path'].split('?')[0])
    key=(c['method'],canonical)
    if key in seen:continue
    seen.add(key)
    md.extend(['## '+c['case'],'',f'`{c["method"]} http://127.0.0.1:8080{c["path"]}`',''])
    if c['request'] is None:md.extend(['No payload.',''])
    else:
        body=dict(c['request'])
        for key in ['password','currentPassword','newPassword','confirmPassword']:
            if key in body:body[key]='TEMPORARY_PASSWORD_FROM_EMAIL' if key=='currentPassword' or (key=='password' and c['case']=='First login requires reset') else 'Example-password-123'
        md.extend(['```json',json.dumps(body,indent=2,ensure_ascii=False),'```',''])
    result=c['response']['results'] if 'results' in c['response'] else c['response']
    if not isinstance(result,(dict,list)):md.extend(['Expected `results`: `'+json.dumps(result)+'`.',''])
Path('docs/api-test-payloads.md').write_text('\n'.join(md),encoding='utf-8')
print(f'Wrote ordered HTTP collection ({len(selected)} requests) and {len(seen)} URL/payload examples.')
