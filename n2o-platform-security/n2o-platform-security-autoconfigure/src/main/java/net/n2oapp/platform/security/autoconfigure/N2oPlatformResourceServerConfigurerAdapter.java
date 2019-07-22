package net.n2oapp.platform.security.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

public class N2oPlatformResourceServerConfigurerAdapter extends ResourceServerConfigurerAdapter {
    @Autowired
    private N2oPlatformSecurityProperties securityProperties;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources)
            throws Exception {
        resources.resourceId(securityProperties.getResourceId());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        configure(http.authorizeRequests());
    }

    /**
     * Настройка авторизации запросов
     * @param requests Запросы
     */
    public void configure(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests) {
        requests.anyRequest().authenticated();
    }

    public void setSecurityProperties(N2oPlatformSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }
}
