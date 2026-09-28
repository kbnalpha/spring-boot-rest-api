-- Run once in MySQL Workbench, or rely on createDatabaseIfNotExist in the JDBC URL.
-- Application tables are created by Liquibase when the application starts.
CREATE DATABASE IF NOT EXISTS `ehs_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
