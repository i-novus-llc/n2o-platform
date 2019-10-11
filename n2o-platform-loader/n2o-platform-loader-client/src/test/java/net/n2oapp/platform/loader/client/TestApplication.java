package net.n2oapp.platform.loader.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
class TestApplication {

    @Bean
    JsonClientLoader jsonClientLoader() {
        return new JsonClientLoader(new RestTemplate(), new ObjectMapper());
    }

    @Bean
    SimpleClientLoader simpleClientLoader() {
        return new SimpleClientLoader();
    }
}
