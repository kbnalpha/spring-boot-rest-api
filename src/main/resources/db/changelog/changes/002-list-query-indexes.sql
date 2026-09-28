--liquibase formatted sql

-- Separate migration demonstrates how subsequent schema changes are tracked.

--changeset ehspro:002-department-list-index dbms:mysql,h2
CREATE INDEX `ix_department_org_created` ON `department` (`business_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_department_org_created` ON `department`;

--changeset ehspro:002-designation-list-index dbms:mysql,h2
CREATE INDEX `ix_designation_org_created` ON `designation` (`business_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_designation_org_created` ON `designation`;

--changeset ehspro:002-employee-list-index dbms:mysql,h2
CREATE INDEX `ix_employee_org_created` ON `employee` (`organization_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_employee_org_created` ON `employee`;

--changeset ehspro:002-location-list-index dbms:mysql,h2
CREATE INDEX `ix_location_org_created` ON `location` (`organization_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_location_org_created` ON `location`;

--changeset ehspro:002-operation_activity-list-index dbms:mysql,h2
CREATE INDEX `ix_operation_activity_org_created` ON `operation_activity` (`business_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_operation_activity_org_created` ON `operation_activity`;

--changeset ehspro:002-observation_type-list-index dbms:mysql,h2
CREATE INDEX `ix_observation_type_org_created` ON `observation_type` (`business_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_observation_type_org_created` ON `observation_type`;

--changeset ehspro:002-equipment-list-index dbms:mysql,h2
CREATE INDEX `ix_equipment_org_created` ON `equipment` (`organization_unit_id`, `created_date`, `id`);
--rollback DROP INDEX `ix_equipment_org_created` ON `equipment`;

--changeset ehspro:002-employee-membership-index dbms:mysql,h2
CREATE INDEX `ix_employee_organization_org` ON `employee_organization` (`organization_id`, `employee_id`);
--rollback DROP INDEX `ix_employee_organization_org` ON `employee_organization`;

