--liquibase formatted sql

-- Initial schema derived from the JPA entities. Run through Liquibase, not by hand.
-- JSON converters use LONGTEXT, matching the entities' @Lob String storage.
-- One changeset per table limits partial-migration recovery for MySQL auto-commit DDL.

--changeset ehspro:001-01-organization_unit dbms:mysql,h2
CREATE TABLE `organization_unit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `name` VARCHAR(2000) NULL,
    `description` TEXT NULL,
    `tenant_id` BIGINT NULL,
    `parent_id` BIGINT NULL,
    `status` INT NULL,
    `currency` VARCHAR(2000) NULL,
    `line1` TEXT NULL,
    `line2` TEXT NULL,
    `city` BIGINT NULL,
    `state` BIGINT NULL,
    `country` BIGINT NULL,
    `country_code` VARCHAR(2000) NULL,
    `symbol` VARCHAR(2000) NULL,
    `time_zone` VARCHAR(2000) NULL,
    `time_zone_id` BIGINT NULL,
    `language_id` BIGINT NULL,
    `key_contact_name` VARCHAR(2000) NULL,
    `phone_number` VARCHAR(2000) NULL,
    `email_address` VARCHAR(2000) NULL,
    `is_anonymous` BIT(1) NULL,
    `is_observation_proof_required` BIT(1) NULL,
    `attachments` ${largeTextType} NULL,
    `shifts` ${largeTextType} NULL,
    `bu_image` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_organization_unit_organization` FOREIGN KEY (`parent_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `organization_unit`;

--changeset ehspro:001-02-department dbms:mysql,h2
CREATE TABLE `department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `name` VARCHAR(2000) NULL,
    `description` VARCHAR(2000) NULL,
    `status` INT NULL,
    `business_unit_id` BIGINT NULL,
    `translations` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_department_organization` FOREIGN KEY (`business_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `department`;

--changeset ehspro:001-03-designation dbms:mysql,h2
CREATE TABLE `designation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `name` VARCHAR(2000) NULL,
    `description` VARCHAR(2000) NULL,
    `status` INT NULL,
    `business_unit_id` BIGINT NULL,
    `translations` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_designation_organization` FOREIGN KEY (`business_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `designation`;

--changeset ehspro:001-04-role dbms:mysql,h2
CREATE TABLE `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `name` VARCHAR(2000) NULL,
    `display_name` VARCHAR(2000) NULL,
    `status` INT NULL,
    `role_description` VARCHAR(2000) NULL,
    `role_type` VARCHAR(2000) NULL,
    `landing_page_id` BIGINT NULL,
    `permissions` ${largeTextType} NULL,
    `role_organization_units` ${largeTextType} NULL,
    `user_roles` ${largeTextType} NULL,
    PRIMARY KEY (`id`)
);
--rollback DROP TABLE `role`;

--changeset ehspro:001-05-employee dbms:mysql,h2
CREATE TABLE `employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `first_name` VARCHAR(2000) NULL,
    `middle_name` VARCHAR(2000) NULL,
    `last_name` VARCHAR(2000) NULL,
    `email_address` VARCHAR(2000) NULL,
    `phone_number` VARCHAR(2000) NULL,
    `profile_picture_id` BIGINT NULL,
    `gender` INT NULL,
    `department` BIGINT NULL,
    `date_of_joining` DATE NULL,
    `contractor_id` BIGINT NULL,
    `access_failed_count` INT NULL,
    `designation` BIGINT NULL,
    `user_type` INT NULL,
    `status` INT NULL,
    `user_number` VARCHAR(100) NOT NULL,
    `tenant_id` BIGINT NULL,
    `language_id` BIGINT NULL,
    `organization_unit_id` BIGINT NULL,
    `has_access` BIT(1) NULL,
    `is_mobile_user` BIT(1) NULL,
    `alias` VARCHAR(2000) NULL,
    `date_of_birth` DATE NULL,
    `age` INT NULL,
    `country` BIGINT NULL,
    `is_subscribed` BIT(1) NULL,
    `user_role_ids` ${largeTextType} NULL,
    `organization_unit_ids_mapped` ${largeTextType} NULL,
    `organization_unit_list_ids` ${largeTextType} NULL,
    `uploaded_files` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_employee_user_number` UNIQUE (`user_number`),
    CONSTRAINT `fk_employee_organization` FOREIGN KEY (`organization_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `employee`;

--changeset ehspro:001-06-location dbms:mysql,h2
CREATE TABLE `location` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `name` VARCHAR(2000) NULL,
    `location_description` VARCHAR(2000) NULL,
    `status` INT NULL,
    `organization_unit_id` BIGINT NULL,
    `supervisor_ids` ${largeTextType} NULL,
    `translations` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_location_organization` FOREIGN KEY (`organization_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `location`;

--changeset ehspro:001-07-sub_location dbms:mysql,h2
CREATE TABLE `sub_location` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NULL,
    `description` VARCHAR(255) NULL,
    `status` INT NULL,
    `location_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_sub_location_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
);
--rollback DROP TABLE `sub_location`;

--changeset ehspro:001-08-operation_activity dbms:mysql,h2
CREATE TABLE `operation_activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `activity_name` VARCHAR(2000) NULL,
    `business_unit_id` BIGINT NULL,
    `category_id` BIGINT NULL,
    `description` VARCHAR(2000) NULL,
    `status` INT NULL,
    `translations` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_operation_activity_organization` FOREIGN KEY (`business_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `operation_activity`;

--changeset ehspro:001-09-observation_type dbms:mysql,h2
CREATE TABLE `observation_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `observation_category_id` BIGINT NULL,
    `type_description` VARCHAR(2000) NULL,
    `enable_svt` BIT(1) NULL,
    `status` INT NULL,
    `business_unit_id` BIGINT NULL,
    `is_default` BIT(1) NULL,
    `translations` ${largeTextType} NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_observation_type_organization` FOREIGN KEY (`business_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `observation_type`;

--changeset ehspro:001-10-observation_sub_type dbms:mysql,h2
CREATE TABLE `observation_sub_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sub_type_description` VARCHAR(255) NULL,
    `status` INT NULL,
    `observation_type_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_observation_sub_type_parent` FOREIGN KEY (`observation_type_id`) REFERENCES `observation_type` (`id`)
);
--rollback DROP TABLE `observation_sub_type`;

--changeset ehspro:001-11-equipment dbms:mysql,h2
CREATE TABLE `equipment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_by` BIGINT NULL,
    `created_date` DATETIME(6) NULL,
    `modified_by` BIGINT NULL,
    `modified_date` DATETIME(6) NULL,
    `equipment_category_id` BIGINT NULL,
    `equipment_type_id` BIGINT NULL,
    `organization_unit_id` BIGINT NULL,
    `uid` VARCHAR(100) NOT NULL,
    `manufacturer` VARCHAR(2000) NULL,
    `model_number` VARCHAR(2000) NULL,
    `serial_number` VARCHAR(2000) NULL,
    `yearof_manufacture` VARCHAR(2000) NULL,
    `status` INT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_equipment_org_uid` UNIQUE (`organization_unit_id`, `uid`),
    CONSTRAINT `fk_equipment_organization` FOREIGN KEY (`organization_unit_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `equipment`;

--changeset ehspro:001-12-employee_organization dbms:mysql,h2
CREATE TABLE `employee_organization` (
    `employee_id` BIGINT NOT NULL,
    `organization_id` BIGINT NOT NULL,
    CONSTRAINT `uk_employee_organization` UNIQUE (`employee_id`, `organization_id`),
    CONSTRAINT `fk_employee_organization_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
    CONSTRAINT `fk_employee_organization_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization_unit` (`id`)
);
--rollback DROP TABLE `employee_organization`;

