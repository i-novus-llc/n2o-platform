package net.n2oapp.platform.i18n.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки интернационализации
 */
@ConfigurationProperties("i18n")
public class I18nProperties {

    private Global global = new Global();

    /**
     * Настройки глобального источника сообщений {@link org.springframework.context.MessageSource}
     */
    public static class Global {
        /**
         * Включение глобального источника сообщений
         */
        private boolean enabled = true;
        /**
         * Пакет, в глобальном classpath, содержащий файлы локализации (*.properties)
         */
        private String packageName = "messages";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    public Global getGlobal() {
        return global;
    }

    public void setGlobal(Global global) {
        this.global = global;
    }
}
