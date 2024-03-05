package net.n2oapp.platform.jaxrs.autoconfigure;

import brave.Tracing;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import net.n2oapp.platform.i18n.Messages;
import net.n2oapp.platform.jaxrs.MessageExceptionMapper;
import net.n2oapp.platform.jaxrs.ViolationRestExceptionMapper;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.jaxrs.swagger.ui.SwaggerUiConfig;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.spring.boot.autoconfigure.CxfProperties;
import org.apache.cxf.tracing.brave.jaxrs.BraveFeature;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.validation.BeanValidationInInterceptor;
import org.apache.cxf.validation.BeanValidationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.validation.ValidatorFactory;

import java.util.Map;
import java.util.Set;

/**
 * Автоматическая конфигурация REST сервисов
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass({ SpringBus.class, CXFServlet.class })
@AutoConfigureBefore(org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration.class)
@EnableConfigurationProperties(JaxRsProperties.class)
public class JaxRsServerAutoConfiguration {

    private final JaxRsProperties jaxRsProperties;
    private final CxfProperties cxfProperties;


    public JaxRsServerAutoConfiguration(CxfProperties cxfProperties, JaxRsProperties jaxRsProperties) {
        this.cxfProperties = cxfProperties;
        this.jaxRsProperties = jaxRsProperties;
    }

    @Bean("openApiFeature")
    @ConditionalOnProperty(prefix = "jaxrs.openapi", name = "enabled", matchIfMissing = true)
    OpenApiFeature openApiFeature() {
        final OpenApiFeature feature = new OpenApiFeature();
        feature.setTitle(jaxRsProperties.getOpenapi().getTitle());
        feature.setDescription(jaxRsProperties.getOpenapi().getDescription());
//        feature.setBasePath(cxfProperties.getPath()); todo test with root and custom base paths. remove from properties
        feature.setVersion(jaxRsProperties.getOpenapi().getVersion());
//        feature.setSchemes(jaxRsProperties.getOpenApi().getSchemes()); todo test with multiple schemes. remove from properties
        feature.setPrettyPrint(true);
        JaxRsProperties.OpenApi.Auth auth = jaxRsProperties.getOpenapi().getAuth();
        if (auth != null && auth.getName() != null && auth.getTokenUri() != null) {
            SecurityScheme securityScheme = getSecurityScheme(auth);
            feature.setSecurityDefinitions(Map.of(auth.getName(), securityScheme));
        }
        if (jaxRsProperties.getOpenapi().getResourcePackages() != null)
            feature.setResourcePackages(Set.of(jaxRsProperties.getOpenapi().getResourcePackages()));
        feature.setScan(true);
/* todo try without this customizer
        OpenApiCustomizer customizer = new OpenApiCustomizer();
        customizer.setDynamicBasePath(true);
        feature.setCustomizer(customizer);
*/

        //Since Swagger UI 4.1.3 disable reading config params from URL by default due to security concerns.
        SwaggerUiConfig swaggerUiConfig = new SwaggerUiConfig();
        swaggerUiConfig.setQueryConfigEnabled(false);
        swaggerUiConfig.setUrl("swagger.json");
        feature.setSwaggerUiConfig(swaggerUiConfig);

        return feature;
    }

    private static SecurityScheme getSecurityScheme(JaxRsProperties.OpenApi.Auth auth) {
        OAuthFlows oAuthFlows = new OAuthFlows();
        OAuthFlow oAuthFlow = new OAuthFlow().refreshUrl(auth.getRefreshUri()).scopes(auth.getScopes());

        switch (auth.getFlow()) {
            case "implicit" -> oAuthFlows.setImplicit(oAuthFlow.authorizationUrl(auth.getAuthorizationUri()));
            case "password" -> oAuthFlows.setPassword(oAuthFlow.tokenUrl(auth.getTokenUri()));
            case "clientCredentials" -> oAuthFlows.setClientCredentials(oAuthFlow.tokenUrl(auth.getTokenUri()));
            case "authorizationCode" -> oAuthFlows.setAuthorizationCode(oAuthFlow.authorizationUrl(auth.getAuthorizationUri()).tokenUrl(auth.getTokenUri()));
        }

        return new SecurityScheme().flows(oAuthFlows).type(SecurityScheme.Type.OAUTH2);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jaxrs", name = {"log-in", "logging-in.enabled"}, matchIfMissing = true)
    LoggingInInterceptor loggingInInterceptor() {
        AnnotatedLoggingInInterceptor loggingInInterceptor = new AnnotatedLoggingInInterceptor();
        loggingInInterceptor.setLimit(jaxRsProperties.getLoggingIn().getLimit());
        loggingInInterceptor.setInMemThreshold(jaxRsProperties.getLoggingIn().getInMemThreshold());
        loggingInInterceptor.setLogBinary(jaxRsProperties.getLoggingIn().isLogBinary());
        loggingInInterceptor.setLogMultipart(jaxRsProperties.getLoggingIn().isLogMultipart());
        loggingInInterceptor.setPrettyLogging(jaxRsProperties.getLoggingIn().isPrettyLogging());
        return loggingInInterceptor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "jaxrs", name = {"log-out", "logging-out.enabled"}, matchIfMissing = true)
    LoggingOutInterceptor loggingOutInterceptor() {
        LoggingOutInterceptor loggingOutInterceptor = new AnnotatedLoggingOutInterceptor();
        loggingOutInterceptor.setLimit(jaxRsProperties.getLoggingOut().getLimit());
        loggingOutInterceptor.setInMemThreshold(jaxRsProperties.getLoggingOut().getInMemThreshold());
        loggingOutInterceptor.setLogBinary(jaxRsProperties.getLoggingOut().isLogBinary());
        loggingOutInterceptor.setLogMultipart(jaxRsProperties.getLoggingOut().isLogMultipart());
        loggingOutInterceptor.setPrettyLogging(jaxRsProperties.getLoggingOut().isPrettyLogging());
        return loggingOutInterceptor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "jaxrs", name = "jsr303", matchIfMissing = true)
    BeanValidationInInterceptor beanValidationInInterceptor(ValidatorFactory validatorFactory) {
        JAXRSBeanValidationInInterceptor validationInInterceptor = new JAXRSBeanValidationInInterceptor();
        BeanValidationProvider validationProvider = new BeanValidationProvider(validatorFactory);
        validationInInterceptor.setProvider(validationProvider);
        return validationInInterceptor;
    }

    @Bean
    ViolationRestExceptionMapper violationExceptionMapper() {
        return new ViolationRestExceptionMapper();
    }

    @Bean
    @ConditionalOnClass(Messages.class)
    RestServerExceptionMapper restServerExceptionMapper(Messages messages, @Value("${n2o.ui.message.stacktrace}") Boolean canExportStack) {
        return new RestServerExceptionMapper(canExportStack, messages);
    }

    @Bean
    @ConditionalOnClass(Messages.class)
    MessageExceptionMapper messageExceptionMapper(Messages messages) {
        return new MessageExceptionMapper(messages);
    }

    @Bean
    @ConditionalOnProperty(value = {"management.tracing.enabled"})
    BraveFeature braveFeature(Tracing brave) {
        return new BraveFeature(brave);
    }

    @Bean
    JaxRsAcceptHeaderSorter jaxRsAcceptHeaderSorter() {
        return new JaxRsAcceptHeaderSorter();
    }

}
