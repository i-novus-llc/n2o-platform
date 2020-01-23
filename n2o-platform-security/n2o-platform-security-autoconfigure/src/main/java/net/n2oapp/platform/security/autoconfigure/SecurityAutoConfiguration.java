package net.n2oapp.platform.security.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(N2oPlatformSecurityProperties.class)
public class SecurityAutoConfiguration {

    private N2oPlatformSecurityProperties securityProperties;

    public SecurityAutoConfiguration(N2oPlatformSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public PlatformRestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
                                                   OAuth2ProtectedResourceDetails details) {
        PlatformRestTemplate restTemplate = new PlatformRestTemplate(details, oauth2ClientContext);
        restTemplate.setRetryBadAccessTokens(false);
        restTemplate.setCheckTokenExpired(securityProperties.isCheckTokenExpired());
        return restTemplate;
    }

    /**
     * Контекст текущего пользователя с токеном
     */
    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public DefaultOAuth2ClientContext oauth2ClientContext(PlatformAccessTokenConverter accessTokenConverter) {
        DefaultOAuth2ClientContext context = new DefaultOAuth2ClientContext(
                new DefaultAccessTokenRequest());
        Authentication principal = SecurityContextHolder.getContext()
                .getAuthentication();
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            Object details = authentication.getDetails();
            if (details instanceof OAuth2AuthenticationDetails) {
                OAuth2AuthenticationDetails oauthsDetails = (OAuth2AuthenticationDetails) details;
                String tokenValue = oauthsDetails.getTokenValue();
                if (Boolean.FALSE.equals(securityProperties.isCheckTokenExpired())) {
                    //Если нет проверки срока действия токена, то создаем простой токен без срока действия
                    context.setAccessToken(new DefaultOAuth2AccessToken(tokenValue));
                } else {
                    //Если есть проверка срока действия токена, то получаем настроящий токен
                    OAuth2AccessToken accessToken = accessTokenConverter.extractAccessToken(tokenValue,
                            accessTokenConverter.decode(tokenValue));
                    context.setAccessToken(accessToken);
                }
            }
        }
        return context;
    }

    /**
     * Бин простой конфертации токенов с методом decode
     */
    @Bean
    public PlatformAccessTokenConverter platformAccessTokenConverter(UserAuthenticationConverter userAuthenticationConverter) {
        PlatformAccessTokenConverter platformAccessTokenConverter = new PlatformAccessTokenConverter();
        platformAccessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
        return platformAccessTokenConverter;
    }

    /**
     * Информация о клиенте OAuth2 при использовании grant_type=client_credentials*
     */
    @Bean
    public OAuth2ProtectedResourceDetails clientCredentialsResourceDetails() {
        ClientCredentialsResourceDetails clientCredentials = new ClientCredentialsResourceDetails();
        clientCredentials.setClientId(securityProperties.getClientId());
        clientCredentials.setClientSecret(securityProperties.getClientSecret());
        clientCredentials.setAccessTokenUri(securityProperties.getAccessTokenUri());
        return clientCredentials;
    }

    /**
     * Бин конвертации аутентификации в информацию о токене и обратно
     */
    @Bean
    @ConditionalOnMissingBean
    public UserAuthenticationConverter n2oPlatformAuthenticationConverter() {
        SimpleAttributes2GrantedAuthoritiesMapper authoritiesMapper = new SimpleAttributes2GrantedAuthoritiesMapper();
        authoritiesMapper.setAttributePrefix(securityProperties.getAuthoritiesPrefix());
        authoritiesMapper.setConvertAttributeToUpperCase(securityProperties.isAuthoritiesUpperCase());
        authoritiesMapper.setConvertAttributeToLowerCase(securityProperties.isAuthoritiesLowerCase());
        return new N2oPlatformAuthenticationConverter(securityProperties.getUsernameKey(), securityProperties.getAuthoritiesKey(), authoritiesMapper);
    }

    @Bean
    public ResourceServerTokenServices tokenServices(TokenStore tokenStore) {
        DefaultTokenServices defaultTokenServices;
        if (Boolean.FALSE.equals(securityProperties.isCheckTokenExpired())) {
            //Если нет проверки срока действия токена, то только читаем токен
            defaultTokenServices = new DefaultTokenServices() {
                @Override
                public OAuth2Authentication loadAuthentication(String accessTokenValue) {
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
            //Если есть проверка срока действия токена, то читаем и проверяем срок действия токена
            defaultTokenServices = new DefaultTokenServices();
        }
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenStore tokenStore(UserAuthenticationConverter userAuthenticationConverter) {
        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter() {
            @Override
            public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
                if (Boolean.FALSE.equals(securityProperties.isCheckAud())) {
                    map.remove("aud");
                }
                return super.extractAuthentication(map);
            }
        };
        accessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
        Assert.hasText(securityProperties.getKeySetUri(), "Set property `n2o.platform.security.key-set-uri`");
        return new JwkTokenStore(Collections.singletonList(securityProperties.getKeySetUri()),
                accessTokenConverter,
                null);
    }

    @Bean
    @ConditionalOnMissingBean(ResourceServerConfigurer.class)
    public ResourceServerConfigurer n2oPlatformResourceServer() {
        N2oPlatformResourceServerConfigurerAdapter adapter = new N2oPlatformResourceServerConfigurerAdapter();
        adapter.setSecurityProperties(this.securityProperties);
        return adapter;
    }
}
