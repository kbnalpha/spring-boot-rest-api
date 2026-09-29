--liquibase formatted sql

--changeset ehspro:005-delegated-role-administration dbms:mysql,h2
UPDATE permission_definition SET super_admin_only=FALSE WHERE code IN ('ManageRoles','ManageRoleUsers');
INSERT INTO permission_definition (id,code,display_name,module_name,parent_id,business_action,super_admin_only)
VALUES (3357,'ManageExternalCollaborators','Manage Temporary Users','Setup',3330,TRUE,FALSE);

--changeset ehspro:005-contractor-details dbms:mysql,h2
ALTER TABLE contractor ADD COLUMN contractor_code VARCHAR(100);
ALTER TABLE contractor ADD COLUMN services_offered VARCHAR(2000);
ALTER TABLE contractor ADD COLUMN address_line1 VARCHAR(500);
ALTER TABLE contractor ADD COLUMN address_line2 VARCHAR(500);
ALTER TABLE contractor ADD COLUMN postal_code VARCHAR(30);
ALTER TABLE contractor ADD COLUMN primary_contact_person_name VARCHAR(255);
ALTER TABLE contractor ADD COLUMN primary_contact_designation VARCHAR(255);
ALTER TABLE contractor ADD COLUMN phone_number VARCHAR(50);
ALTER TABLE contractor ADD COLUMN email VARCHAR(255);
ALTER TABLE contractor ADD COLUMN website VARCHAR(500);
ALTER TABLE contractor ADD COLUMN linked_in VARCHAR(500);
ALTER TABLE contractor ADD COLUMN country_id BIGINT;
ALTER TABLE contractor ADD COLUMN state_id BIGINT;
ALTER TABLE contractor ADD COLUMN city_id BIGINT;

--changeset ehspro:005-external-collaborators dbms:mysql,h2
CREATE TABLE external_collaborator (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 created_by BIGINT, created_date TIMESTAMP(6), modified_by BIGINT, modified_date TIMESTAMP(6),
 role_id BIGINT NOT NULL, organization_unit_id BIGINT NOT NULL,
 first_name VARCHAR(255) NOT NULL, middle_name VARCHAR(255), last_name VARCHAR(255) NOT NULL,
 email_address VARCHAR(255), phone_number VARCHAR(50), gender INT NOT NULL,
 status INT NOT NULL, alias VARCHAR(255), country BIGINT, company_name VARCHAR(500), designation VARCHAR(255),
 has_access BOOLEAN NOT NULL DEFAULT FALSE, age INT, date_of_birth DATE, date_of_joining DATE,
 CONSTRAINT fk_external_role FOREIGN KEY (role_id) REFERENCES role(id),
 CONSTRAINT fk_external_org FOREIGN KEY (organization_unit_id) REFERENCES organization_unit(id)
);
CREATE INDEX ix_external_org_created ON external_collaborator(organization_unit_id,created_date);
CREATE TABLE external_collaborator_organization (
 external_collaborator_id BIGINT NOT NULL,
 organization_unit_id BIGINT NOT NULL,
 PRIMARY KEY(external_collaborator_id,organization_unit_id),
 CONSTRAINT fk_external_membership_user FOREIGN KEY(external_collaborator_id) REFERENCES external_collaborator(id),
 CONSTRAINT fk_external_membership_org FOREIGN KEY(organization_unit_id) REFERENCES organization_unit(id)
);
