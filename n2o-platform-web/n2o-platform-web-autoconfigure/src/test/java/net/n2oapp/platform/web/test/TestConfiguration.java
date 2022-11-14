package net.n2oapp.platform.web.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import javax.servlet.*;
import java.io.IOException;

@Configuration
public class TestConfiguration {
    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll().and().addFilterBefore(new Filter() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken("user", "N/A");
                authenticationToken.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }, AnonymousAuthenticationFilter.class).csrf().disable();

        return http.build();
    }
}
