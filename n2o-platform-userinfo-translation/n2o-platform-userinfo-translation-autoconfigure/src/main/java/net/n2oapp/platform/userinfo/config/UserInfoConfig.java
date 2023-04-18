package net.n2oapp.platform.userinfo.config;

import net.n2oapp.platform.userinfo.JsonToPrincipalFilter;
import net.n2oapp.platform.userinfo.mapper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserInfoConfig {

    @Value("${n2o.platform.userinfo-translation.header-name:n2o-user-info}")
    private String userInfoHeaderName;

    @Bean
    @ConditionalOnMissingBean(JsonToPrincipalAbstractMapper.class)
    public JsonToPrincipalAbstractMapper jsonToPrincipalMapper() {
        return new JsonToPrincipalMapper();
    }

    @Bean
    @ConditionalOnMissingClass("net.n2oapp.framework.boot.N2oEnvironmentConfiguration")
    public FilterRegistrationBean<JsonToPrincipalFilter> userInfoFilter(JsonToPrincipalAbstractMapper jsonToPrincipalMapper) {
        JsonToPrincipalFilter jsonToPrincipalFilter = new JsonToPrincipalFilter(jsonToPrincipalMapper, userInfoHeaderName);
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(jsonToPrincipalFilter);
        registration.addUrlPatterns("/*");
        registration.setName("userInfoFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    @ConditionalOnClass(name = "net.n2oapp.framework.boot.N2oEnvironmentConfiguration")
    @ConditionalOnMissingBean(PrincipalToJsonAbstractMapper.class)
    public PrincipalToJsonAbstractMapper oauthPrincipalToJsonMapper() {
        return new OauthPrincipalToJsonMapper();
    }

    @Bean
    @ConditionalOnMissingClass("net.n2oapp.framework.boot.N2oEnvironmentConfiguration")
    @ConditionalOnMissingBean(PrincipalToJsonAbstractMapper.class)
    public PrincipalToJsonAbstractMapper userInfoToJsonMapper() {
        return new UserInfoToJsonMapper();
    }
}
