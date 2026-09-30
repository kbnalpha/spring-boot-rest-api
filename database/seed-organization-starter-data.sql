-- First create your organization through the API.
-- Set its ID in the SAME MySQL session before running this file:
-- SET @organization_id = 123;
-- Without a valid organization ID this script inserts nothing.
-- Rerunning preserves existing records matched by organization and name.
START TRANSACTION;
INSERT INTO department (name,description,status,business_unit_id,created_date,translations)
SELECT s.name,s.name,1,o.id,UTC_TIMESTAMP(6),'[]'
FROM organization_unit o
CROSS JOIN (SELECT 'Operations' name UNION ALL SELECT 'Environment, Health and Safety' UNION ALL SELECT 'Human Resources' UNION ALL SELECT 'Maintenance') s
WHERE o.id=@organization_id AND NOT EXISTS
(SELECT 1 FROM department d WHERE d.business_unit_id=o.id AND d.name=s.name);
INSERT INTO designation (name,description,status,business_unit_id,created_date,translations)
SELECT s.name,s.name,1,o.id,UTC_TIMESTAMP(6),'[]'
FROM organization_unit o
CROSS JOIN (SELECT 'Manager' name UNION ALL SELECT 'Safety Officer' UNION ALL SELECT 'Supervisor' UNION ALL SELECT 'Employee') s
WHERE o.id=@organization_id AND NOT EXISTS
(SELECT 1 FROM designation d WHERE d.business_unit_id=o.id AND d.name=s.name);
INSERT INTO location (name,location_description,status,organization_unit_id,created_date,supervisor_ids,translations)
SELECT s.name,'Starter location - edit to match your site',1,o.id,UTC_TIMESTAMP(6),'[]','[]'
FROM organization_unit o
CROSS JOIN (SELECT 'Main Office' name UNION ALL SELECT 'Production Area' UNION ALL SELECT 'Warehouse') s
WHERE o.id=@organization_id AND NOT EXISTS
(SELECT 1 FROM location l WHERE l.organization_unit_id=o.id AND l.name=s.name);
INSERT INTO sub_location (name,description,status,location_id)
SELECT s.child,'Starter sublocation - edit to match your site',1,l.id
FROM location l
JOIN (
 SELECT 'Main Office' parent,'Reception' child UNION ALL
 SELECT 'Main Office','Meeting Room' UNION ALL
 SELECT 'Production Area','Production Line 1' UNION ALL
 SELECT 'Production Area','Maintenance Bay' UNION ALL
 SELECT 'Warehouse','Receiving Area' UNION ALL
 SELECT 'Warehouse','Storage Area'
) s ON s.parent=l.name
WHERE l.organization_unit_id=@organization_id AND NOT EXISTS
(SELECT 1 FROM sub_location sl WHERE sl.location_id=l.id AND sl.name=s.child);
COMMIT;
SELECT id,name FROM department WHERE business_unit_id=@organization_id;
SELECT id,name FROM designation WHERE business_unit_id=@organization_id;
SELECT l.id AS location_id,l.name AS location,sl.id AS sublocation_id,sl.name AS sublocation
FROM location l LEFT JOIN sub_location sl ON sl.location_id=l.id
WHERE l.organization_unit_id=@organization_id;
