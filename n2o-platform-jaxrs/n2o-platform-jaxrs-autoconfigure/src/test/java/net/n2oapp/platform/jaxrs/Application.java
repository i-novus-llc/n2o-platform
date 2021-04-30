package net.n2oapp.platform.jaxrs;

import net.n2oapp.platform.jaxrs.seek.SeekPivotParameterConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    private static final String[] ACCEPT_HEADERS = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML};
    private static final String[] CONTENT_TYPE_HEADERS = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML};

    static final Map<String, String>[] HEADERS = new HashMap[ACCEPT_HEADERS.length * CONTENT_TYPE_HEADERS.length];
    static {
        int paramsIdx = 0;
        for (String accept : ACCEPT_HEADERS) {
            for (String contentType : CONTENT_TYPE_HEADERS) {
                Map<String, String> params = new HashMap<>();
                params.put(HttpHeaders.ACCEPT, accept);
                params.put(HttpHeaders.CONTENT_TYPE, contentType);
                HEADERS[paramsIdx++] = params;
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public SeekPivotParameterConverter seekPivotParameterConverter() {
        return new SeekPivotParameterConverter();
    }

}
