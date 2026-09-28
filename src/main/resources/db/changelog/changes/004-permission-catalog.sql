--liquibase formatted sql

--changeset ehspro:004-business-permissions dbms:mysql,h2
INSERT INTO permission_definition (id,code,display_name,module_name,parent_id,business_action,super_admin_only) VALUES
(1023,'Administration','Administration','Administration',NULL,FALSE,FALSE),
(1024,'Observation','Observation','Observation',NULL,FALSE,FALSE),
(3330,'Setup','Setup','Setup',NULL,FALSE,FALSE),
(5000,'Incident','Incident','Incident',NULL,FALSE,FALSE),
(3272,'ManageRoles','Manage Roles','Administration',1023,TRUE,TRUE),
(3274,'ManageRoleUsers','Manage Role Users','Administration',1023,TRUE,TRUE),
(3301,'ManageBusinessUnits','Manage Business Units','Administration',1023,TRUE,TRUE),
(3302,'CreateRole','Create Role','Administration',1023,TRUE,FALSE),
(3303,'ViewRoles','View Roles','Administration',1023,TRUE,FALSE),
(3304,'ViewSystemUsers','View System Users','Administration',1023,TRUE,FALSE),
(3334,'ManageEmployees','Manage Employees','Setup',3330,TRUE,FALSE),
(3342,'ManageDepartment','Manage Department','Setup',3330,TRUE,FALSE),
(3341,'ManageDesignation','Manage Designation','Setup',3330,TRUE,FALSE),
(3335,'ManageEquipment','Manage Equipment','Setup',3330,TRUE,FALSE),
(3336,'ManageLocations','Manage Locations','Setup',3330,TRUE,FALSE),
(3337,'ManageOperationalActivities','Manage Operational Activities','Setup',3330,TRUE,FALSE),
(3338,'ObservationTypes','Observation Types','Setup',3330,TRUE,FALSE),
(3355,'ManageContractEmployees','Manage Contract Employees','Setup',3330,TRUE,FALSE),
(3356,'ManageContractors','Manage Contractors','Setup',3330,TRUE,FALSE),
(2031,'CreateObservation','Create Observation','Observation',1024,TRUE,FALSE),
(2032,'ReviewObservation','Review Observation','Observation',1024,TRUE,FALSE),
(2033,'ViewObservation','View Observation','Observation',1024,TRUE,FALSE),
(5001,'CreateIncident','Create Incident','Incident',5000,TRUE,FALSE),
(5002,'ViewIncident','View Incident','Incident',5000,TRUE,FALSE),
(5003,'EditIncident','Edit Incident','Incident',5000,TRUE,FALSE),
(5004,'ReviewIncident','Review Incident','Incident',5000,TRUE,FALSE),
(5005,'ApproveIncident','Approve Incident','Incident',5000,TRUE,FALSE),
(5006,'CloseIncident','Close Incident','Incident',5000,TRUE,FALSE);

--changeset ehspro:004-system-super-admin dbms:mysql,h2
INSERT INTO role (id,name,display_name,status,role_description,landing_page_id,tenant_id,system_role)
VALUES (-1,'Super Admin','Super Admin',1,'Backend system role; not configurable through application APIs',1,0,TRUE);
