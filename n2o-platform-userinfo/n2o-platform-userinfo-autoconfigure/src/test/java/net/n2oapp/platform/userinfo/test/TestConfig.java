package net.n2oapp.platform.userinfo.test;

import net.n2oapp.platform.userinfo.UserInfo;
import net.n2oapp.platform.userinfo.mapper.OauthPrincipalToJsonMapper;
import net.n2oapp.platform.userinfo.mapper.PrincipalToJsonAbstractMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@TestConfiguration

public class TestConfig {

    @RestController
    @RequestMapping
    public class TestController {

        @GetMapping("/")
        public Boolean testEndpoint() {
            return SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserInfo;
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll();
        return http.build();
    }

    @Bean
    @Primary
    public PrincipalToJsonAbstractMapper oauthPrincipalToJsonMapper() {
        return new OauthPrincipalToJsonMapper();
    }
}