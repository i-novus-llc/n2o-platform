package net.n2oapp.platform.jaxrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    private static final String[] ACCEPT_HEADERS = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML};
    private static final String[] CONTENT_TYPE_HEADERS = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML};

    public static final String ACCEPT_HEADER_KEY = "Accept";
    public static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";

    public static final Map[] PARAMS = new HashMap[ACCEPT_HEADERS.length * CONTENT_TYPE_HEADERS.length];
    static {
        int paramsIdx = 0;
        for (String accept : ACCEPT_HEADERS) {
            for (String contentType : CONTENT_TYPE_HEADERS) {
                Map<String, String> params = new HashMap<>();
                params.put(ACCEPT_HEADER_KEY, accept);
                params.put(CONTENT_TYPE_HEADER_KEY, contentType);
                PARAMS[paramsIdx++] = params;
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
