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
    private boolean logIn = true;
    /**
     * Логирование всех ответов
     */
    private boolean logOut = true;
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

    public boolean isJsr303() {
        return jsr303;
    }

    public void setJsr303(boolean jsr303) {
        this.jsr303 = jsr303;
    }
}
