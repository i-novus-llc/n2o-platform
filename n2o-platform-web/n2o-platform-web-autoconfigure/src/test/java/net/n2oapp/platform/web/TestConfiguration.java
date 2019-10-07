package net.n2oapp.platform.web;

import net.n2oapp.criteria.dataset.DataSet;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@Configuration
@RestController
public class TestConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll();
    }

    @GetMapping("/token")
    public DataSet token(HttpServletRequest request) {
        return new DataSet("token", Collections.singletonList(Map.of("auth", request.getHeader("authorization"))));
    }
}
