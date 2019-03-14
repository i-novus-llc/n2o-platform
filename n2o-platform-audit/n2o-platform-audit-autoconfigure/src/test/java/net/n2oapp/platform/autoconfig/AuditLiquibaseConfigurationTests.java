package net.n2oapp.platform.autoconfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootApplication
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {AuditLiquibaseConfigurationTests.class, H2JpaConfig.class, AuditLiquibaseConfiguration.class})
public class AuditLiquibaseConfigurationTests {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    ApplicationContext context;

    @Test
    public void auditLiquibaseExist() {
        Assert.assertNotNull(context.getBean("auditLiquibase"));
    }

    @Test
    public void emptyLiquibaseBean() {
        Assert.assertFalse(context.getBean("liquibase") instanceof Nullable);
    }

    @Test
    public void testDBChangelog() {
        Assert.assertTrue((Boolean) entityManager.createNativeQuery("SELECT EXISTS (SELECT 1 FROM information_schema.tables " +
                "WHERE  table_schema = 'PUBLIC' AND table_name = 'DATABASECHANGELOG')").getSingleResult());
    }
}
