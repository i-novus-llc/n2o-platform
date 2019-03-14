package net.n2oapp.platform.autoconfig;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
@AutoConfigureAfter(LiquibaseAutoConfiguration.class)
public class AuditLiquibaseConfiguration {

    private final DataSource dataSource;

    @Autowired
    public AuditLiquibaseConfiguration(ObjectProvider<DataSource> dataSource) {
        this.dataSource = dataSource.getIfUnique();
    }

    @Bean(name = "liquibase")
    @ConditionalOnMissingBean(SpringLiquibase.class)
    public void liquibase() {
        //если в приложении не используется liquibase (обязательный bean)
    }

    @Bean(name = "auditLiquibase")
    @DependsOn("liquibase")
    public SpringLiquibase auditLiquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/db/changelog/audit-changelog.yaml");
        return liquibase;
    }
}
