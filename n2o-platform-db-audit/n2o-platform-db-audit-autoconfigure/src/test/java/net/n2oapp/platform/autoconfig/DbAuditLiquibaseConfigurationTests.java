package net.n2oapp.platform.autoconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

@SpringBootApplication
@SpringBootTest(classes = {DbAuditLiquibaseConfigurationTests.class, DbAuditLiquibaseConfiguration.class})
public class DbAuditLiquibaseConfigurationTests {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ApplicationContext context;

    @Test
    public void auditLiquibaseExist() {
        Assertions.assertNotNull(context.getBean("auditLiquibase"));
    }

    @Test
    public void emptyLiquibaseBean() {
        Assertions.assertFalse(context.getBean("liquibase") instanceof Nullable);
    }

    @Test
    public void testDBChangelog() {
        Assertions.assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM information_schema.tables " +
                "WHERE  table_schema = 'PUBLIC' AND table_name = 'DATABASECHANGELOG')", Boolean.class));
    }
}