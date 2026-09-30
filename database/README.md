# MySQL schema and Liquibase workflow

The application uses Aiven MySQL at `mysql-ehs-kbnalpha-bbce.l.aivencloud.com:15129`, database `ehs_db`, username `avnadmin`. The mysql profile imports the project-root `.env` as a properties file. The password is provided through `DB_PASSWORD`; no password is committed. Copy `.env.example` for a new checkout and fill the password locally. Environment variables override these settings.

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
2. Create `src/main/resources/db/changelog/changes/010-location-code.sql` (003 through 009 implement the workbook, RBAC, and complete master contracts):

```sql
--liquibase formatted sql

--changeset ehspro:010-location-code dbms:mysql,h2
ALTER TABLE location ADD COLUMN code VARCHAR(50) NULL;
--rollback ALTER TABLE location DROP COLUMN code;
```

3. Append this include to `db.changelog-master.yaml`:

```yaml
  - include:
      file: changes/010-location-code.sql
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

The original baseline records 20 changesets: 12 tables and 8 indexes. The master-field/RBAC additions bring the total to 41 changesets and 24 tables, including Liquibase's two tables. See [the master/RBAC guide](master-fields-rbac.md) for the new schema. Later startups validate checksums and skip changesets already applied. History tracks migration execution, not row edits or arbitrary manual DDL. Hibernate validation catches missing/incompatible mapped columns but is not a complete database drift audit.

Some earlier migrations supply rollback SQL; migration 005 is forward-only. Use reviewed compensating migrations for later changes. Table rollback removes data; normal application startup only migrates forward and never executes rollback. MySQL DDL commits implicitly, so failed multi-step DDL cannot be assumed to roll back transactionally. Each initial changeset creates one table, and each index changeset creates one index to limit partial execution.

For an existing database populated outside Liquibase, reconcile its schema against the baseline before adopting it. The initial migration intentionally does not silently mark existing tables as migrated. Existing data is not dropped or overwritten automatically.

## Verification

`mvn verify` migrates H2 in MySQL compatibility mode and validates JPA mappings, APIs, security, and migration idempotency. Set `MYSQL_MIGRATION_TEST=true` to also apply/check migrations on the configured MySQL database, verify repeat updates, and round-trip Unicode, JSON, long text, and child relationships. Test records in MySQL are transactionally rolled back; migration history and schema remain.

References: [Spring Boot 3.2 Liquibase integration](https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase), [Liquibase formatted SQL](https://www.liquibase.com/blog/liquibase-formatted-sql).

## Aiven connection

The JDBC URL requires TLS (`sslMode=REQUIRED`), with a pool of at most five connections. This encrypts traffic but does not verify the server certificate identity. For certificate and hostname verification, obtain the Aiven project CA, configure a Connector/J truststore, and override `DB_URL` with `sslMode=VERIFY_IDENTITY` and the appropriate truststore properties. See [Aiven TLS certificates](https://aiven.io/docs/platform/concepts/tls-ssl-certificates) and [Connector/J TLS configuration](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html).

The configured JDBC URL creates `ehs_db` if permitted, then Liquibase creates/migrates the tables. Existing local MySQL data is not copied to Aiven by changing the connection settings. Run `database/create-database.sql` on Aiven if you prefer to create the database explicitly.

## Aiven primary-key enforcement

Aiven requires primary keys, but Liquibase initially creates `DATABASECHANGELOG` without one. The immutable baseline also creates `employee_organization` with a unique constraint instead of a primary key.

The mysql profile gives Liquibase a separate JDBC connection with `sessionVariables=sql_require_primary_key=0`, the [session-level override documented by Aiven](https://aiven.io/docs/products/mysql/howto/create-tables-without-primary-keys). This lets Liquibase bootstrap its history and run the baseline. Migration 009 then adds real composite primary keys to both tables. Existing primary keys are detected and retained. Applied migrations 001?008 and their checksums are unchanged.

The override applies only to Liquibase connections. It does not change Aiven's global setting or normal Hikari application connections. Every table has a primary key after migration 009. Do not put the override in `DB_URL` or disable the server-wide setting.

`LIQUIBASE_DB_URL` can override the migration-specific URL if needed. By default it appends the session property to the application's JDBC URL, which must already contain a query string (the supplied default does). If supplying a bare `DB_URL` without `?`, also supply a complete `LIQUIBASE_DB_URL` with `?sessionVariables=sql_require_primary_key=0` and the required TLS settings.
