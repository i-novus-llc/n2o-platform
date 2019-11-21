package net.n2oapp.platform.loader.client.auth;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Шаблон вызова Rest запросов с передачей токена доступа
 */
public class AuthRestTemplate extends RestTemplate {

    private Map<String, ClientContext> contextStorage;

    public AuthRestTemplate(Map<String, ClientContext> contextStorage) {
        this.contextStorage = contextStorage;
    }

    @Override
    protected ClientHttpRequest createRequest(URI uri, HttpMethod method) throws IOException {
        ClientHttpRequest req = super.createRequest(uri, method);

        if (contextStorage != null) {
            ClientContext ctx = contextStorage.get(uri.toString());
            if (ctx != null) {
                String accessToken = ctx.getAccessToken();
                if (accessToken != null)
                    req.getHeaders().set("Authorization", String.format("%s %s", ctx.getTokenType(), accessToken));

            }
        }

        return req;
    }
}
