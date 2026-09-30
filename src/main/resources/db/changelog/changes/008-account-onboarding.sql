--liquibase formatted sql

--changeset ehspro:008-account-onboarding dbms:mysql,h2
ALTER TABLE user_account MODIFY COLUMN username VARCHAR(254) NOT NULL;
ALTER TABLE user_account ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE user_account ADD COLUMN temporary_password_expires_at TIMESTAMP(6) NULL;
ALTER TABLE user_account ADD COLUMN password_changed_at TIMESTAMP(6) NULL;
