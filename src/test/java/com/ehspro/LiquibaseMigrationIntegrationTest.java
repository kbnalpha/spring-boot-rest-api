package com.ehspro;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LiquibaseMigrationIntegrationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired SpringLiquibase liquibase;

    @Test
    void schemaChangesAreRecordedAndSecondUpdateDoesNotReapplyThem() throws Exception {
        var before = jdbc.queryForList("SELECT ID, AUTHOR, FILENAME, MD5SUM, DATEEXECUTED, ORDEREXECUTED FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED");
        assertThat(before).hasSize(38);
        assertThat(before).allSatisfy(row -> assertThat(row.get("MD5SUM")).isNotNull());
        liquibase.afterPropertiesSet();
        var after = jdbc.queryForList("SELECT ID, AUTHOR, FILENAME, MD5SUM, DATEEXECUTED, ORDEREXECUTED FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED");
        assertThat(after).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT LOCKED FROM DATABASECHANGELOGLOCK WHERE ID = 1", Boolean.class)).isFalse();
    }
}
