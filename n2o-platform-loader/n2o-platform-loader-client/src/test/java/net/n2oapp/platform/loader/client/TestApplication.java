package net.n2oapp.platform.loader.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@SpringBootApplication
@RestController
class TestApplication {

    @Bean
    JsonClientLoader jsonClientLoader() {
        return new JsonClientLoader(new RestTemplate());
    }

    @Bean
    SimpleClientLoader simpleClientLoader() {
        return new SimpleClientLoader();
    }


    @PostMapping("/token")
    public Map<String, Object> tokenEndpoint(HttpServletRequest request) {
        if ("client_credentials".equals(request.getParameter("grant_type"))
                && "Basic dGVzdENsaWVudDp0ZXN0Q2xpZW50U2VjcmV0".equals(request.getHeader("Authorization"))) {
            return Map.of("access_token", "test_token",
                    "token_type", "bearer");
        }

        return null;
    }

    @GetMapping("/resource")
    public String resourceServer(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    @PostMapping("/hello")
    public String hello() {
        return "hello";
    }
}
