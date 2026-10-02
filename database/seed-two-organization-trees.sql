-- Optional sample organization trees; preserves existing records.

START TRANSACTION;

INSERT INTO organization_unit (name,description,tenant_id,parent_id,status,line1,city,state,country,country_code,currency,symbol,time_zone,time_zone_id,language_id,key_contact_name,phone_number,email_address,is_anonymous,is_observation_proof_required,shifts,created_date)
SELECT 'EHS Group A','Sample organization for Admin scope testing',1001,NULL,1,'Sample office address, Hyderabad',1201,1101,1001,'91','INR','?','Asia/Kolkata',1401,1301,'Sample Contact','0000000000','contact@example.com',0,1,'[]',UTC_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM organization_unit WHERE name='EHS Group A' AND tenant_id=1001);

INSERT INTO organization_unit (name,description,tenant_id,parent_id,status,line1,city,state,country,country_code,currency,symbol,time_zone,time_zone_id,language_id,key_contact_name,phone_number,email_address,is_anonymous,is_observation_proof_required,shifts,created_date)
SELECT 'EHS Group A - Child','Sample organization for Admin scope testing',1001,(SELECT id FROM (SELECT id FROM organization_unit WHERE name='EHS Group A' AND tenant_id=1001) parent_rows),1,'Sample office address, Hyderabad',1201,1101,1001,'91','INR','?','Asia/Kolkata',1401,1301,'Sample Contact','0000000000','contact@example.com',0,1,'[]',UTC_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM organization_unit WHERE name='EHS Group A - Child' AND tenant_id=1001);

INSERT INTO organization_unit (name,description,tenant_id,parent_id,status,line1,city,state,country,country_code,currency,symbol,time_zone,time_zone_id,language_id,key_contact_name,phone_number,email_address,is_anonymous,is_observation_proof_required,shifts,created_date)
SELECT 'EHS Group B','Sample organization for Admin scope testing',1002,NULL,1,'Sample office address, Hyderabad',1201,1101,1001,'91','INR','?','Asia/Kolkata',1401,1301,'Sample Contact','0000000000','contact@example.com',0,1,'[]',UTC_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM organization_unit WHERE name='EHS Group B' AND tenant_id=1002);

INSERT INTO organization_unit (name,description,tenant_id,parent_id,status,line1,city,state,country,country_code,currency,symbol,time_zone,time_zone_id,language_id,key_contact_name,phone_number,email_address,is_anonymous,is_observation_proof_required,shifts,created_date)
SELECT 'EHS Group B - Child','Sample organization for Admin scope testing',1002,(SELECT id FROM (SELECT id FROM organization_unit WHERE name='EHS Group B' AND tenant_id=1002) parent_rows),1,'Sample office address, Hyderabad',1201,1101,1001,'91','INR','?','Asia/Kolkata',1401,1301,'Sample Contact','0000000000','contact@example.com',0,1,'[]',UTC_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM organization_unit WHERE name='EHS Group B - Child' AND tenant_id=1002);

INSERT INTO department (name,description,business_unit_id,status,translations,created_date)
SELECT 'Operations','Sample master data',o.id,1,'[]',UTC_TIMESTAMP(6) FROM organization_unit o
WHERE o.tenant_id IN (1001,1002) AND o.name IN ('EHS Group A','EHS Group A - Child','EHS Group B','EHS Group B - Child')
AND NOT EXISTS (SELECT 1 FROM department d WHERE d.business_unit_id=o.id AND d.name='Operations');

INSERT INTO designation (name,description,business_unit_id,status,translations,created_date)
SELECT 'Administrator','Sample master data',o.id,1,'[]',UTC_TIMESTAMP(6) FROM organization_unit o
WHERE o.tenant_id IN (1001,1002) AND o.name IN ('EHS Group A','EHS Group A - Child','EHS Group B','EHS Group B - Child')
AND NOT EXISTS (SELECT 1 FROM designation d WHERE d.business_unit_id=o.id AND d.name='Administrator');

INSERT INTO designation (name,description,business_unit_id,status,translations,created_date)
SELECT 'Employee','Sample master data',o.id,1,'[]',UTC_TIMESTAMP(6) FROM organization_unit o
WHERE o.tenant_id IN (1001,1002) AND o.name IN ('EHS Group A','EHS Group A - Child','EHS Group B','EHS Group B - Child')
AND NOT EXISTS (SELECT 1 FROM designation d WHERE d.business_unit_id=o.id AND d.name='Employee');

COMMIT;

SELECT id,name,tenant_id,parent_id FROM organization_unit WHERE tenant_id IN (1001,1002) ORDER BY tenant_id,id;
