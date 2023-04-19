package net.n2oapp.platform.userinfo.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import net.n2oapp.platform.userinfo.TranslateUserInfoHolder;
import net.n2oapp.platform.userinfo.UserInfoTranslationAdvice;
import net.n2oapp.platform.userinfo.mapper.PrincipalToJsonAbstractMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static java.util.Objects.isNull;

@Configuration
public class InterceptorConfig {

    @Value("${n2o.platform.userinfo.default-behavior:true}")
    private Boolean userinfoTranslationDefaultBehavior;

    @Value("${n2o.platform.userinfo.header-name:n2o-user-info}")
    private String userInfoHeaderName;

    @Bean
    public ExchangeFilterFunction userinfoExchangeFilterFunction(PrincipalToJsonAbstractMapper principalToJsonMapper) {
        return (request, next) -> {
            addUserInfoHeader(request.headers(), principalToJsonMapper);
            return next.exchange(request);
        };
    }

    @Bean
    public ClientHttpRequestInterceptor userinfoRestTemplateInterceptor(PrincipalToJsonAbstractMapper principalToJsonMapper) {
        return (request, body, execution) -> {
            addUserInfoHeader(request.getHeaders(), principalToJsonMapper);
            return execution.execute(request, body);
        };
    }

    @Bean
    public RequestInterceptor userinfoFeignInterceptor(PrincipalToJsonAbstractMapper principalToJsonMapper) {
        return template -> addUserInfoHeader(template, principalToJsonMapper);
    }

    private void addUserInfoHeader(Object httpHeaders, PrincipalToJsonAbstractMapper principalMapper) {
        Boolean translateUserInfo = TranslateUserInfoHolder.get();
        if ((userinfoTranslationDefaultBehavior && translateUserInfo) || translateUserInfo) {
            SecurityContext context = SecurityContextHolder.getContext();
            if (isNull(context))
                return;
            Authentication authentication = context.getAuthentication();
            if (isNull(authentication))
                return;
            Object principal = authentication.getPrincipal();
            if (isNull(principal))
                return;
            if (httpHeaders instanceof HttpHeaders)
                HttpHeaders.writableHttpHeaders(((HttpHeaders) httpHeaders)).add(userInfoHeaderName, principalMapper.map(principal));
            else if (httpHeaders instanceof RequestTemplate) {
                ((RequestTemplate) httpHeaders).header(userInfoHeaderName, principalMapper.map(principal));
            }
        }
    }

    @Bean
    public UserInfoTranslationAdvice userInfoTranslationAdvice() {
        return new UserInfoTranslationAdvice();
    }

    @Bean
    public RestTemplate userInfoTranslationRestTemplate(@Qualifier("userinfoRestTemplateInterceptor") ClientHttpRequestInterceptor interceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(interceptor));
        return restTemplate;
    }

    @Bean
    public WebClient userInfoTranslationWebClient(@Qualifier("userinfoExchangeFilterFunction") ExchangeFilterFunction userinfoExchangeFilterFunction) {
        return WebClient.builder().filter(userinfoExchangeFilterFunction).build();
    }
}
