# MySQL schema and Liquibase workflow

The application uses MySQL database `ehs_db` at `localhost:3306`, with username `root` and password `root`. Both credentials can be overridden through `DB_USERNAME` and `DB_PASSWORD`.

## Initial schema

Start the application with `mvn spring-boot:run`. The JDBC URL creates the database if it does not exist, Liquibase applies the migrations, and Hibernate validates the resulting schema. Alternatively, execute [create-database.sql](create-database.sql) in MySQL Workbench before starting the application.

The authoritative SQL schema is [001-initial-schema.sql](../src/main/resources/db/changelog/changes/001-initial-schema.sql), followed by [002-list-query-indexes.sql](../src/main/resources/db/changelog/changes/002-list-query-indexes.sql). The master YAML includes them in order. The `largeTextType` placeholder is `LONGTEXT` on MySQL and `CLOB` on H2, so the same migrations are tested without maintaining a second schema.

The initial schema contains these 12 tables:

| Table | Entity or collection |
| --- | --- |
| organization_unit | OrganizationUnit |
| department | Department |
| designation | Designation |
| role | Role |
| employee | Employee |
| employee_organization | Employee organization membership |
| location | Location |
| sub_location | SubLocation |
| operation_activity | OperationActivity |
| observation_type | ObservationType |
| observation_sub_type | ObservationSubType |
| equipment | Equipment |

Primary keys use `BIGINT AUTO_INCREMENT`. Audit dates use `DATETIME(6)`. Boolean fields use `BIT(1)`. JSON-converted values use `LONGTEXT` to match the existing JPA converters. Employee numbers are unique; equipment UIDs are unique within their organization. Foreign keys protect organization references, location children, observation children, and employee organization memberships. Department/designation/contractor IDs on employees retain their existing scalar semantics, including sentinel 0; no foreign keys point to unspecified lookup catalogs.

Organization descriptions and address lines use `TEXT` to keep the utf8mb4 table below MySQL's maximum row size while retaining the API's 2000-character limit. JSON LOB fields explicitly declare their maximum column length so Hibernate expects MySQL `LONGTEXT` rather than its default `TINYTEXT`. Employee `tenantID` and `languageID` have explicit column mappings to `tenant_id` and `language_id`.

## Alter a table

For example, if a future feature adds a nullable `code` field to Location:

1. Add the entity field and any DTO/service changes.
2. Create `src/main/resources/db/changelog/changes/008-location-code.sql` (003 through 007 implement the workbook, RBAC, and complete master contracts):

```sql
--liquibase formatted sql

--changeset ehspro:008-location-code dbms:mysql,h2
ALTER TABLE location ADD COLUMN code VARCHAR(50) NULL;
--rollback ALTER TABLE location DROP COLUMN code;
```

3. Append this include to `db.changelog-master.yaml`:

```yaml
  - include:
      file: changes/008-location-code.sql
      relativeToChangelogFile: true
```

4. Run `mvn verify`, then restart the application. Liquibase records and executes the new changeset once. This is an example only; no unused `code` column is included in the delivered schema.

Use a new changeset for every alteration. Do not edit an already applied migration, move/rename it, set `runOnChange` on table migrations, or delete history records to bypass a mismatch. Before making a new column non-null on an existing populated table, add it nullable, backfill existing rows, then apply the non-null constraint in a later changeset.

## Inspect history

```sql
USE ehs_db;

SELECT ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM
FROM DATABASECHANGELOG
ORDER BY ORDEREXECUTED;

SELECT ID, LOCKED, LOCKGRANTED, LOCKEDBY
FROM DATABASECHANGELOGLOCK;
```

The original baseline records 20 changesets: 12 tables and 8 indexes. The master-field/RBAC additions bring the total to 38 changesets and 24 tables, including Liquibase's two tables. See [the master/RBAC guide](master-fields-rbac.md) for the new schema. Later startups validate checksums and skip changesets already applied. History tracks migration execution, not row edits or arbitrary manual DDL. Hibernate validation catches missing/incompatible mapped columns but is not a complete database drift audit.

Some earlier migrations supply rollback SQL; migration 005 is forward-only. Use reviewed compensating migrations for later changes. Table rollback removes data; normal application startup only migrates forward and never executes rollback. MySQL DDL commits implicitly, so failed multi-step DDL cannot be assumed to roll back transactionally. Each initial changeset creates one table, and each index changeset creates one index to limit partial execution.

For an existing database populated outside Liquibase, reconcile its schema against the baseline before adopting it. The initial migration intentionally does not silently mark existing tables as migrated. Existing data is not dropped or overwritten automatically.

## Verification

`mvn verify` migrates H2 in MySQL compatibility mode and validates JPA mappings, APIs, security, and migration idempotency. Set `MYSQL_MIGRATION_TEST=true` to also apply/check migrations on the configured MySQL database, verify repeat updates, and round-trip Unicode, JSON, long text, and child relationships. Test records in MySQL are transactionally rolled back; migration history and schema remain.

References: [Spring Boot 3.2 Liquibase integration](https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase), [Liquibase formatted SQL](https://www.liquibase.com/blog/liquibase-formatted-sql).
