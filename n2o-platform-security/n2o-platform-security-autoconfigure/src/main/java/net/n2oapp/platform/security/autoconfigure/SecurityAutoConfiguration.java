package net.n2oapp.platform.security.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;
import org.springframework.util.Assert;

import java.util.*;

@Configuration
@EnableConfigurationProperties(N2oPlatformSecurityProperties.class)
public class SecurityAutoConfiguration {

    private N2oPlatformSecurityProperties securityProperties;

    public SecurityAutoConfiguration(N2oPlatformSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
                                                 OAuth2ProtectedResourceDetails details) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(details, oauth2ClientContext);
        restTemplate.setRetryBadAccessTokens(false);
        return restTemplate;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public DefaultOAuth2ClientContext oauth2ClientContext() {
        DefaultOAuth2ClientContext context = new DefaultOAuth2ClientContext(
                new DefaultAccessTokenRequest());
        Authentication principal = SecurityContextHolder.getContext()
                .getAuthentication();
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            Object details = authentication.getDetails();
            if (details instanceof OAuth2AuthenticationDetails) {
                OAuth2AuthenticationDetails oauthsDetails = (OAuth2AuthenticationDetails) details;
                String token = oauthsDetails.getTokenValue();
                context.setAccessToken(new DefaultOAuth2AccessToken(token));
            }
        }
        return context;
    }

    @Bean
    public OAuth2ProtectedResourceDetails clientCredentialsResourceDetails() {
        ClientCredentialsResourceDetails clientCredentials = new ClientCredentialsResourceDetails();
        clientCredentials.setClientId(securityProperties.getClientId());
        clientCredentials.setClientSecret(securityProperties.getClientSecret());
        clientCredentials.setAccessTokenUri("http://172.16.1.130:8888/auth/realms/MPS/protocol/openid-connect/token");
        return clientCredentials;
    }

    @Bean
    public N2oPlatformAuthenticationConverter n2oPlatformAuthenticationConverter() {
        return new N2oPlatformAuthenticationConverter();
    }

    @Bean
    public ResourceServerTokenServices tokenServices(TokenStore tokenStore) {
        DefaultTokenServices defaultTokenServices;
        if (Boolean.FALSE.equals(securityProperties.isCheckTokenExpired())) {
            defaultTokenServices = new DefaultTokenServices() {
                @Override
                public OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException, InvalidTokenException {
                    OAuth2AccessToken accessToken = tokenStore.readAccessToken(accessTokenValue);
                    if (accessToken == null) {
                        throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
                    }
                    OAuth2Authentication result = tokenStore.readAuthentication(accessToken);
                    if (result == null) {
                        throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
                    }
                    return result;
                }
            };
        } else {
            defaultTokenServices = new DefaultTokenServices();
        }
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenStore tokenStore(UserAuthenticationConverter userAuthenticationConverter) {
        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
        Assert.hasText(securityProperties.getKeySetUri(), "Set property `n2o.platform.security.key-set-uri`");
        return new JwkTokenStore(Collections.singletonList(securityProperties.getKeySetUri()),
                accessTokenConverter,
                null);
    }

    @Bean
    @ConditionalOnMissingBean(ResourceServerConfigurer.class)
    public ResourceServerConfigurer n2oPlatformResourceServer() {
        return new SecurityAutoConfiguration.ResourceSecurityConfigurer(this.securityProperties.getResourceId());
    }

    protected static class ResourceSecurityConfigurer
            extends ResourceServerConfigurerAdapter {

        private String resourceId;

        public ResourceSecurityConfigurer(String resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources)
                throws Exception {
            resources.resourceId(this.resourceId);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().authenticated().and().httpBasic().disable();
        }
    }
}
