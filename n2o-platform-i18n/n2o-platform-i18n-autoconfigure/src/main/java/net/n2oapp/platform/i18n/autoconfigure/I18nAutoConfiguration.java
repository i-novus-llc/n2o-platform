package net.n2oapp.platform.i18n.autoconfigure;

import net.n2oapp.platform.i18n.Messages;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Автоматическая конфигурация интернационализации
 */
@Configuration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
public class I18nAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "i18n", name = "global.enabled", matchIfMissing = true)
    @EnableConfigurationProperties({I18nProperties.class, MessageSourceAutoConfiguration.class})
    public static class GlobalMessageSourceConfiguration {
        private I18nProperties i18nProperties;
        private MessageSourceAutoConfiguration messageSourceProperties;

        public GlobalMessageSourceConfiguration(I18nProperties i18nProperties,
                                     MessageSourceAutoConfiguration messageSourceProperties) {
            this.i18nProperties = i18nProperties;
            this.messageSourceProperties = messageSourceProperties;
        }

        @Bean
        MessageSource messageSource() throws IOException {
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
            List<String> baseNames = new ArrayList<>();
            if (StringUtils.hasText(messageSourceProperties.getBasename())) {
                baseNames.addAll((StringUtils.commaDelimitedListToSet(
                        StringUtils.trimAllWhitespace(messageSourceProperties.getBasename()))));
            }
            baseNames.addAll(scanBaseNames(i18nProperties.getGlobal().getPackageName()));
            messageSource.setBasenames(baseNames.toArray(new String[baseNames.size()]));
            if (messageSourceProperties.getEncoding() != null) {
                messageSource.setDefaultEncoding(messageSourceProperties.getEncoding().name());
            }
            messageSource.setFallbackToSystemLocale(messageSourceProperties.isFallbackToSystemLocale());
            messageSource.setCacheSeconds(messageSourceProperties.getCacheSeconds());
            messageSource.setAlwaysUseMessageFormat(messageSourceProperties.isAlwaysUseMessageFormat());
            return messageSource;
        }

        private Set<String> scanBaseNames(String packageName) throws IOException {
            Set<String> baseNames = new HashSet<>();
            String pack = (!packageName.endsWith("/") ? packageName + "/" : packageName);
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource[] resources = r.getResources("classpath*:" + pack + "*.properties");
            for (Resource resource : resources) {
                int endIdx = resource.getFilename().indexOf('_');
                if (endIdx < 0) {
                    endIdx = resource.getFilename().indexOf(".properties");
                }
                baseNames.add(pack + resource.getFilename().substring(0, endIdx));
            }
            return baseNames;
        }

    }

    @Bean
    @ConditionalOnMissingBean
    MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
        return new MessageSourceAccessor(messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    Messages messages(MessageSourceAccessor messageSourceAccessor) {
        return new Messages(messageSourceAccessor);
    }
}
