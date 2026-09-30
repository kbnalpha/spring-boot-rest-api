--liquibase formatted sql

--changeset ehspro:007-built-in-admin dbms:mysql,h2
ALTER TABLE role ADD COLUMN built_in_admin BOOLEAN NOT NULL DEFAULT FALSE;
INSERT INTO role (name,display_name,status,role_description,landing_page_id,tenant_id,system_role,built_in_admin)
VALUES ('Admin','Admin',1,'Organization-scoped administrator; cannot create root organizations or Super Admin identities',1,0,TRUE,TRUE);
