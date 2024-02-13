package net.n2oapp.platform.jaxrs.autoconfigure;

import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Настройки JaxRS конфигурации
 */
@ConfigurationProperties(prefix = "jaxrs")
public class JaxRsProperties {
    /**
     * Логирование всех запросов
     */
    private Logging loggingIn = new Logging();
    /**
     * Логирование всех ответов
     */
    private Logging loggingOut = new Logging();

    public static class Logging {
        private boolean enabled = true;
        //The size limit at which messages are truncated in the log. The default is unlimited.
        private int limit = -1;
        //Size limit when messages are written to disk. The default is 100kb.
        private long inMemThreshold = 100L * 1024;
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
     * Настройки OpenApi
     */
    private final OpenApi openApi = new OpenApi();

    public static class OpenApi {

        /**
         * Включить/выключить OpenApi
         */
        private boolean enabled = true;

        /**
         * Заголовок главной страницы
         */
        private String title;

        /**
         * Описание
         */
        private String description;

        /**
         * Версия OpenApi
         */
        private String version;

        /**
         * Сканируемый пакет
         */
        private String[] resourcePackages;

        /**
         * Доступные схемы запросов
         */
        private String[] schemes = new String[] {"http", "https"};

        /**
         * Настройки авторизации
         * Протокол - OAuth2, возможные типы - Client credentials, Resource Owner Password Credentials
         */
        private Auth auth;

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

        public String[] getResourcePackages() {
            return resourcePackages;
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

        public void setResourcePackages(String[] resourcePackages) {
            this.resourcePackages = resourcePackages;
        }

        public String[] getSchemes() {
            return schemes;
        }

        public void setSchemes(String[] schemes) {
            this.schemes = schemes;
        }

        public Auth getAuth() {
            return auth;
        }

        public void setAuth(Auth auth) {
            this.auth = auth;
        }

        public static class Auth {
            /**
             * Наименование способа авторизации
             * Должно совпадать с аргументом value в аннотации
             * {@link io.swagger.annotations.Authorization} на стороне сервиса
             */
            private String name;

            /**
             * Конечная точка сервиса предоставления токена
             */
            private String tokenUri;

            private String authorizationUri;

            private String refreshUri;

            private Scopes scopes;

            /**
             * Флоу (implicit, password, clientCredentials, authorizationCode)
             * <a href="https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#oauth-flows-object">...</a>
             */
            private String flow = "password";

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getTokenUri() {
                return tokenUri;
            }

            public void setTokenUri(String tokenUri) {
                this.tokenUri = tokenUri;
            }

            public String getFlow() {
                return flow;
            }

            public void setFlow(String flow) {
                this.flow = flow;
            }

            public String getAuthorizationUri() {
                return authorizationUri;
            }

            public void setAuthorizationUri(String authorizationUri) {
                this.authorizationUri = authorizationUri;
            }

            public String getRefreshUri() {
                return refreshUri;
            }

            public void setRefreshUri(String refreshUri) {
                this.refreshUri = refreshUri;
            }

            public Scopes getScopes() {
                return scopes;
            }

            public void setScopes(Scopes scopes) {
                this.scopes = scopes;
            }
        }
    }

    public OpenApi getOpenApi() {
        return openApi;
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
