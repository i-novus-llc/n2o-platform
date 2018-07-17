package net.n2oapp.platform.test.autoconfigure;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import net.n2oapp.platform.test.PortFinder;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import ru.inovus.util.pg.embeded.PatchedPgBinaryResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Configuration
public class EmbeddedPgAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedPgAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "test", name = "embedded-pg", havingValue = "true")
    public DataSource dataSource() {
        return new EmbeddedDataSourceFactory().getEmbeddedDatabase();
    }

    @Bean
    public static EmbeddedDataSourceBeanFactoryPostProcessor embeddedDataSourceBeanFactoryPostProcessor() {
        return new EmbeddedDataSourceBeanFactoryPostProcessor();
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    private static class EmbeddedDataSourceBeanFactoryPostProcessor
            implements BeanDefinitionRegistryPostProcessor {

        private static final Logger logger = LoggerFactory
                .getLogger(EmbeddedDataSourceBeanFactoryPostProcessor.class);

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            Assert.isInstanceOf(ConfigurableListableBeanFactory.class, registry,
                    "Test Database Auto-configuration can only be "
                            + "used with a ConfigurableListableBeanFactory");
            process(registry, (ConfigurableListableBeanFactory) registry);
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            // Do nothing
        }

        private void process(BeanDefinitionRegistry registry,
                             ConfigurableListableBeanFactory beanFactory) {
            BeanDefinitionHolder holder = getDataSourceBeanDefinition(beanFactory);
            if (holder != null) {
                String beanName = holder.getBeanName();
                boolean primary = holder.getBeanDefinition().isPrimary();
                logger.info("Replacing '{}' DataSource bean with {} embedded version", beanName, primary ? "primary " : "");
                registry.registerBeanDefinition(beanName,
                        createEmbeddedBeanDefinition(primary));
            }
        }

        private BeanDefinition createEmbeddedBeanDefinition(boolean primary) {
            BeanDefinition beanDefinition = new RootBeanDefinition(
                    EmbeddedDataSourceFactoryBean.class);
            beanDefinition.setPrimary(primary);
            return beanDefinition;
        }

        private BeanDefinitionHolder getDataSourceBeanDefinition(
                ConfigurableListableBeanFactory beanFactory) {
            String[] beanNames = beanFactory.getBeanNamesForType(DataSource.class);
            if (ObjectUtils.isEmpty(beanNames)) {
                logger.warn("No DataSource beans found, "
                        + "embedded version will not be used");
                return null;
            }
            if (beanNames.length == 1) {
                String beanName = beanNames[0];
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                return new BeanDefinitionHolder(beanDefinition, beanName);
            }
            for (String beanName : beanNames) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                if (beanDefinition.isPrimary()) {
                    return new BeanDefinitionHolder(beanDefinition, beanName);
                }
            }
            logger.warn("No primary DataSource found, "
                    + "embedded version will not be used");
            return null;
        }

    }

    private static class EmbeddedDataSourceFactoryBean
            implements FactoryBean<DataSource>, EnvironmentAware, InitializingBean {

        private EmbeddedDataSourceFactory factory;

        private DataSource embeddedDatabase;

        @Override
        public void setEnvironment(Environment environment) {
            this.factory = new EmbeddedDataSourceFactory();
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            this.embeddedDatabase = this.factory.getEmbeddedDatabase();
        }

        @Override
        public DataSource getObject() throws Exception {
            return this.embeddedDatabase;
        }

        @Override
        public Class<?> getObjectType() {
            return PGSimpleDataSource.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

    }

    private static class EmbeddedDataSourceFactory {

        public DataSource getEmbeddedDatabase(){
            int port = PortFinder.getPort();
            String dbName = "db_"+port;

            EmbeddedPostgres pg = null;
            try {

                pg = EmbeddedPostgres.builder().setPgBinaryResolver(new PatchedPgBinaryResolver()).setCleanDataDirectory(true).setPort(port).start();
            } catch (IOException e) {
                logger.error("cannot build EmbeddedPostgres", e);
            }
            DataSource dataSource = pg.getPostgresDatabase();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "DROP TEXT SEARCH CONFIGURATION IF EXISTS ru; " +
                                 "DROP TEXT SEARCH DICTIONARY IF EXISTS ispell_ru; " +
                                 "CREATE TEXT SEARCH DICTIONARY ispell_ru (\n" +
                                 "template= ispell,\n" +
                                 "dictfile= ru,\n" +
                                 "afffile=ru,\n" +
                                 "stopwords = russian\n" +
                                 ");\n" +
                                 "CREATE TEXT SEARCH CONFIGURATION ru ( COPY = russian );\n" +
                                 "ALTER TEXT SEARCH CONFIGURATION ru\n" +
                                 "ALTER MAPPING\n" +
                                 "FOR word, hword, hword_part\n" +
                                 "WITH ispell_ru, russian_stem; " +
                                 "DROP DATABASE IF EXISTS " + dbName + "; CREATE DATABASE " + dbName + ";"
                 );) {
                preparedStatement.executeUpdate();
                DataSource ds = pg.getDatabase("postgres", dbName);
                try (
                        Connection userDbCon = ds.getConnection();
                        PreparedStatement dictPreparedStatement = userDbCon.prepareStatement(
                                "DROP TEXT SEARCH CONFIGURATION IF EXISTS ru; " +
                                        "DROP TEXT SEARCH DICTIONARY IF EXISTS ispell_ru; " +
                                        "CREATE TEXT SEARCH DICTIONARY ispell_ru (\n" +
                                        "template= ispell,\n" +
                                        "dictfile= ru,\n" +
                                        "afffile=ru,\n" +
                                        "stopwords = russian\n" +
                                        ");\n" +
                                        "CREATE TEXT SEARCH CONFIGURATION ru ( COPY = russian );\n" +
                                        "ALTER TEXT SEARCH CONFIGURATION ru\n" +
                                        "ALTER MAPPING\n" +
                                        "FOR word, hword, hword_part\n" +
                                        "WITH ispell_ru, russian_stem; ")
                ) {
                    dictPreparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                logger.error("cannot init db", e);
            }


            return pg.getDatabase("postgres", dbName);

        }
    }
}


