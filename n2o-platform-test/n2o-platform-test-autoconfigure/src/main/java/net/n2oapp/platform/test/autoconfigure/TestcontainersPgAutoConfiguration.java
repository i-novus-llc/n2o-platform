package net.n2oapp.platform.test.autoconfigure;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Configuration
public class TestcontainersPgAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TestcontainersPgAutoConfiguration.class);

    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";
    private static final int PSQL_PORT = 5432;

    private final static WaitStrategy PG_WAIT_STRATEGY = new LogMessageWaitStrategy()
            .withRegEx(".*database system is ready to accept connections.*\\s")
            .withTimes(2)
            .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));

    private static int testcontainersPgImageVersion;

    @Value("${testcontainers.pg.version:12}")
    public void setEnv(final int version){
        TestcontainersPgAutoConfiguration.testcontainersPgImageVersion = version;
    }

    @Bean
    public static TestcontainersPgDataSourceBeanFactoryPostProcessor testcontainersPgDataSourceBeanFactoryPostProcessor() {
        return new TestcontainersPgDataSourceBeanFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "test", name = "testcontainers-pg", havingValue = "true")
    public DataSource dataSource() {
        return new TestcontainersPgDataSourceFactory(psqlContainer()
                , USERNAME
                , PASSWORD)
                .getTestcontainersPgDatabase();
    }

    @Bean(destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "test", name = "testcontainers-pg", havingValue = "true")
    static GenericContainer psqlContainer() {
        final GenericContainer gc = new GenericContainer<>(DockerImageName.parse(generatePgImageName()))
                .withEnv("POSTGRES_USER", USERNAME)
                .withEnv("POSTGRES_PASSWORD", PASSWORD)
                .withExposedPorts(PSQL_PORT);
        gc.setWaitStrategy(PG_WAIT_STRATEGY);
        return gc;
    }

    private final static String generatePgImageName(){
        if(testcontainersPgImageVersion >= 10 && testcontainersPgImageVersion <= 12) {
            return ("inovus/postgres:" + testcontainersPgImageVersion + "-textsearch-ru");
        } else {
            return ("inovus/postgres:12-textsearch-ru");
        }
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    private static class TestcontainersPgDataSourceBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

        private static final Logger logger = LoggerFactory.getLogger(TestcontainersPgDataSourceBeanFactoryPostProcessor.class);

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            Assert.isInstanceOf(ConfigurableListableBeanFactory.class, registry, "Testcontainers PG Database Auto-configuration can only be used with a ConfigurableListableBeanFactory");
            process(registry, (DefaultListableBeanFactory) registry);
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {}

        private void process(BeanDefinitionRegistry registry, DefaultListableBeanFactory beanFactory) {
            beanFactory.setAllowBeanDefinitionOverriding(true);
            final BeanDefinitionHolder holder = getDataSourceBeanDefinition(beanFactory);
            if (holder != null) {
                final String beanName = holder.getBeanName();
                final boolean primary = holder.getBeanDefinition().isPrimary();
                logger.info("Replacing '{}' DataSource bean with {} testcontainers pg version", beanName, primary ? "primary " : "");
                registry.registerBeanDefinition(beanName, createTestcontainersPgBeanDefinition(primary));
            }
        }

        private BeanDefinition createTestcontainersPgBeanDefinition(boolean primary) {
            final BeanDefinition beanDefinition = new RootBeanDefinition(TestContainersPgDataSourceFactoryBean.class);
            beanDefinition.setPrimary(primary);
            return beanDefinition;
        }

        private BeanDefinitionHolder getDataSourceBeanDefinition(ConfigurableListableBeanFactory beanFactory) {
            final String[] beanNames = beanFactory.getBeanNamesForType(DataSource.class);
            if (ObjectUtils.isEmpty(beanNames)) {
                logger.warn("No DataSource beans found, testcontainers version will not be used");
                return null;
            }
            if (beanNames.length == 1) {
                final String beanName = beanNames[0];
                final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                return new BeanDefinitionHolder(beanDefinition, beanName);
            }
            for (final String beanName : beanNames) {
                final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                if (beanDefinition.isPrimary()) { return new BeanDefinitionHolder(beanDefinition, beanName); }
            }
            logger.warn("No primary DataSource found, testcontainers pg version will not be used");
            return null;
        }
    }

    private static class TestContainersPgDataSourceFactoryBean implements FactoryBean<DataSource>, EnvironmentAware, InitializingBean {

        private TestcontainersPgDataSourceFactory factory;
        private DataSource testcontainersPgDatabase;

        @Override
        public void setEnvironment(Environment environment) {
            this.factory = new TestcontainersPgDataSourceFactory(psqlContainer(), USERNAME, PASSWORD);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            this.testcontainersPgDatabase = this.factory.getTestcontainersPgDatabase();
        }

        @Override
        public DataSource getObject() throws Exception {
            return this.testcontainersPgDatabase;
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

    private static class TestcontainersPgDataSourceFactory {

        private final GenericContainer pg;
        private final String username;
        private final String password;
        private final int psqlPort = 5432;

        public TestcontainersPgDataSourceFactory(final GenericContainer pg
                , final String username
                , final String password) {
            this.pg = pg;
            this.username = username;
            this.password = password;
        }

        DataSource getTestcontainersPgDatabase() {
            try {
                pg.start();
            } catch (final Exception e) {
                logger.error("cannot build testcontainers PG", e);
                throw new BeanCreationException("cannot create dataSource", e);
            }
            final String host = pg.getHost();
            final int port = pg.getMappedPort(psqlPort);
            final String dbName = "db_" + port;
            final DataSource dataSource = generateDatasource(host, port, "postgres", username, password);
            try (final Connection connection = dataSource.getConnection();
                 final PreparedStatement preparedStatement = connection.prepareStatement(
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
                 )) {
                preparedStatement.executeUpdate();
                final DataSource ds = generateDatasource(host, port, dbName, username, password);
                try (
                        final Connection userDbCon = ds.getConnection();
                        final PreparedStatement dictPreparedStatement = userDbCon.prepareStatement(
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
            } catch (final SQLException e) {
                logger.error("cannot init db", e);
                throw new BeanCreationException("cannot create datasource", e);
            }
            return generateDatasource(host, port, dbName, username, password);
        }

        public final DataSource generateDatasource(final String host, final Integer port, final String dbName, final String username, final String password) {
            final PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerName(host);
            ds.setPortNumber(port);
            ds.setDatabaseName(dbName);
            ds.setUser(username);
            ds.setPassword(password);
            return ds;
        }
    }
}