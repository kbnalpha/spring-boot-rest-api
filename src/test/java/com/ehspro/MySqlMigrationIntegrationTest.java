package com.ehspro;

import com.ehspro.entity.OrganizationUnit;
import com.ehspro.entity.Location;
import com.ehspro.entity.SubLocation;
import com.ehspro.repository.OrganizationUnitRepository;
import com.ehspro.repository.LocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

/** Opt-in: applies real migrations, but rolls back inserted test records. */
@SpringBootTest
@ActiveProfiles("mysql")
@EnabledIfEnvironmentVariable(named = "MYSQL_MIGRATION_TEST", matches = "true")
class MySqlMigrationIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired SpringLiquibase liquibase;
    @Autowired OrganizationUnitRepository organizations;
    @Autowired LocationRepository locations;
    @Autowired EntityManager entityManager;
    @Autowired ObjectMapper mapper;

    @Test
    void mysqlSchemaValidatesAndMigrationsAreNotReapplied() throws Exception {
        assertThat(jdbc.queryForObject("SELECT @@SESSION.sql_require_primary_key",Integer.class))
            .isEqualTo(jdbc.queryForObject("SELECT @@GLOBAL.sql_require_primary_key",Integer.class));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class)).isEqualTo(41);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables t WHERE t.table_schema=DATABASE() AND t.table_type='BASE TABLE' AND NOT EXISTS (SELECT 1 FROM information_schema.table_constraints c WHERE c.table_schema=t.table_schema AND c.table_name=t.table_name AND c.constraint_type='PRIMARY KEY')",Integer.class)).isZero();
        var before = jdbc.queryForList("SELECT ID, MD5SUM, DATEEXECUTED FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED");
        liquibase.afterPropertiesSet();
        assertThat(jdbc.queryForList("SELECT ID, MD5SUM, DATEEXECUTED FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED")).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()", Integer.class)).isEqualTo(24);
    }

    @Test
    @Transactional
    void mysqlPersistsUnicodeLargeTextJsonAndChildRelationships() throws Exception {
        OrganizationUnit organization = new OrganizationUnit();
        organization.name = "Schema verification \uD83D\uDEE0";
        organization.description = "D".repeat(2000);
        organization.line1 = "A".repeat(2000);
        organization.line2 = "B".repeat(2000);
        organization.attachments = mapper.readTree("{\"name\":\"verification\"}");
        organizations.saveAndFlush(organization);
        Location location = new Location();
        location.name = "Migration verification";
        location.organizationUnitId = organization.id;
        SubLocation child = new SubLocation();
        child.name = "Child";
        location.subLocations.add(child);
        locations.saveAndFlush(location);
        entityManager.clear();
        OrganizationUnit loaded = organizations.findById(organization.id).orElseThrow();
        assertThat(loaded.name).isEqualTo(organization.name);
        assertThat(loaded.description).hasSize(2000);
        assertThat(loaded.attachments.path("name").asText()).isEqualTo("verification");
        assertThat(locations.findById(location.id).orElseThrow().subLocations).hasSize(1);
    }
}
