package net.n2oapp.platform.loader.client.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

/**
 * Контекст OAuth2 клиента
 */
public class OAuth2ClientContext implements ClientContext {

    private String clientId;
    private String clientSecret;
    private String tokenEndpoint;
    private RestTemplate template = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();

    public OAuth2ClientContext(String clientId, String clientSecret, String tokenEndpoint) {
        Assert.notNull(clientId, "ClientId mustn't be null");
        Assert.notNull(clientSecret, "ClientSecret mustn't be null");
        Assert.notNull(tokenEndpoint, "TokenEndpoint mustn't be null");
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpoint = tokenEndpoint;
    }

    @Override
    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<String> resp = template.postForEntity(tokenEndpoint, request, String.class);

        Map<String, Object> body;
        try {
            body = mapper.readValue(resp.getBody(), new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        String accessToken = null;
        if (body != null)
            accessToken = (String) body.get("access_token");

        return accessToken;
    }

    @Override
    public String getTokenType() {
        return "Bearer";
    }
}
