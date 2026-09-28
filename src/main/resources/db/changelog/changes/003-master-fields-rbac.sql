--liquibase formatted sql

--changeset ehspro:003-organization-layout dbms:mysql,h2
ALTER TABLE organization_unit ADD COLUMN layout_image_url TEXT NULL;
ALTER TABLE organization_unit ADD COLUMN latitude DECIMAL(10,7) NULL;
ALTER TABLE organization_unit ADD COLUMN longitude DECIMAL(10,7) NULL;

--changeset ehspro:003-organization-name-limit dbms:mysql,h2
--preconditions onFail:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM organization_unit WHERE CHAR_LENGTH(name) > 50
ALTER TABLE organization_unit MODIFY COLUMN name VARCHAR(50) NULL;

--changeset ehspro:003-employee-contact dbms:mysql,h2
ALTER TABLE employee ADD COLUMN country_code VARCHAR(20) NULL;

--changeset ehspro:003-role-instance dbms:mysql,h2
ALTER TABLE role ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE role ADD COLUMN system_role BOOLEAN NOT NULL DEFAULT FALSE;

--changeset ehspro:003-reference-data dbms:mysql,h2
CREATE TABLE reference_item (
    kind VARCHAR(30) NOT NULL,
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    country_id BIGINT NULL,
    state_id BIGINT NULL,
    code VARCHAR(100) NULL,
    currency VARCHAR(10) NULL,
    symbol VARCHAR(10) NULL,
    zone_id VARCHAR(100) NULL,
    PRIMARY KEY (kind, id)
);
INSERT INTO reference_item (kind, id, name, code) VALUES ('LANDING_PAGE', 1, 'Dashboard', '/dashboard');

--changeset ehspro:003-contractor dbms:mysql,h2
CREATE TABLE contractor (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_by BIGINT NULL, created_date DATETIME(6) NULL,
    modified_by BIGINT NULL, modified_date DATETIME(6) NULL,
    name VARCHAR(255) NOT NULL,
    business_unit_id BIGINT NOT NULL,
    status INT NOT NULL,
    CONSTRAINT fk_contractor_organization FOREIGN KEY (business_unit_id) REFERENCES organization_unit(id)
);

--changeset ehspro:003-permission-catalog dbms:mysql,h2
CREATE TABLE permission_definition (
    id BIGINT NOT NULL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    parent_id BIGINT NULL,
    business_action BOOLEAN NOT NULL,
    super_admin_only BOOLEAN NOT NULL
);

--changeset ehspro:003-role-permissions dbms:mysql,h2
CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT fk_role_permission_definition FOREIGN KEY (permission_id) REFERENCES permission_definition(id)
);

--changeset ehspro:003-system-accounts dbms:mysql,h2
CREATE TABLE user_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    basic_role_id BIGINT NOT NULL,
    CONSTRAINT fk_account_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT fk_account_basic_role FOREIGN KEY (basic_role_id) REFERENCES role(id)
);

--changeset ehspro:003-account-roles dbms:mysql,h2
CREATE TABLE account_role (
    account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, role_id),
    CONSTRAINT fk_account_role_account FOREIGN KEY (account_id) REFERENCES user_account(id),
    CONSTRAINT fk_account_role_role FOREIGN KEY (role_id) REFERENCES role(id)
);

--changeset ehspro:003-account-scope dbms:mysql,h2
CREATE TABLE account_organization_scope (
    account_id BIGINT NOT NULL,
    organization_unit_id BIGINT NOT NULL,
    include_descendants BOOLEAN NOT NULL,
    PRIMARY KEY (account_id, organization_unit_id),
    CONSTRAINT fk_scope_account FOREIGN KEY (account_id) REFERENCES user_account(id),
    CONSTRAINT fk_scope_organization FOREIGN KEY (organization_unit_id) REFERENCES organization_unit(id)
);
