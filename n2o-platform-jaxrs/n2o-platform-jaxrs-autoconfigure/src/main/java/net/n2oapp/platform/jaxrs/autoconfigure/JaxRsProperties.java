package net.n2oapp.platform.jaxrs.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки JaxRS конфигурации
 */
@ConfigurationProperties(prefix = "jaxrs")
public class JaxRsProperties {
    /**
     * Логирование всех запросов
     */
    @Deprecated
    private boolean logIn = true;
    private Logging loggingIn = new Logging();
    /**
     * Логирование всех ответов
     */
    @Deprecated
    private boolean logOut = true;
    private Logging loggingOut = new Logging();

    public static class Logging {
        private boolean enabled = true;
        private int limit = 1024 * 1024;
        private long inMemThreshold = 100 * 1024;
        private boolean prettyLogging;
        private boolean logBinary;
        private boolean logMultipart = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public long getInMemThreshold() {
            return inMemThreshold;
        }

        public void setInMemThreshold(long inMemThreshold) {
            this.inMemThreshold = inMemThreshold;
        }

        public boolean isPrettyLogging() {
            return prettyLogging;
        }

        public void setPrettyLogging(boolean prettyLogging) {
            this.prettyLogging = prettyLogging;
        }

        public boolean isLogBinary() {
            return logBinary;
        }

        public void setLogBinary(boolean logBinary) {
            this.logBinary = logBinary;
        }

        public boolean isLogMultipart() {
            return logMultipart;
        }

        public void setLogMultipart(boolean logMultipart) {
            this.logMultipart = logMultipart;
        }
    }

    /**
     * Включение валидаций JSR303
     */
    private boolean jsr303 = true;

    /**
     * Настройки Swagger
     */
    private final Swagger swagger = new Swagger();

    public static class Swagger {
        private boolean enabled = true;
        private String title;
        private String description;
        private String version;
        private String resourcePackage;

        public boolean isEnabled() {
            return enabled;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getVersion() {
            return version;
        }

        public String getResourcePackage() {
            return resourcePackage;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setResourcePackage(String resourcePackage) {
            this.resourcePackage = resourcePackage;
        }
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public boolean isLogIn() {
        return logIn;
    }

    public void setLogIn(boolean logIn) {
        this.logIn = logIn;
    }

    public boolean isLogOut() {
        return logOut;
    }

    public void setLogOut(boolean logOut) {
        this.logOut = logOut;
    }

    public Logging getLoggingIn() {
        return loggingIn;
    }

    public Logging getLoggingOut() {
        return loggingOut;
    }

    public boolean isJsr303() {
        return jsr303;
    }

    public void setJsr303(boolean jsr303) {
        this.jsr303 = jsr303;
    }
}
