package net.n2oapp.platform.loader.client;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.net.URI;

public abstract class RestClientLoader<T> implements ClientLoader {
    private RestOperations restTemplate;
    private String urlPattern = "/load/{subject}/{target}";

    public RestClientLoader(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestClientLoader(RestOperations restTemplate, String urlPattern) {
        this.restTemplate = restTemplate;
        this.urlPattern = urlPattern;
    }

    @Override
    public void load(URI server, String subject, String target, Resource file) {
        T data = getData(file);
        HttpEntity<T> request = new HttpEntity<>(data, getHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
                getUrl(server, subject, target),
                request,
                String.class);
        if (!response.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Fail: " + response.getBody());
    }

    protected MultiValueMap<String, String> getHeaders() {
        return null;
    };

    protected abstract T getData(Resource file);

    protected String getUrl(URI server, String subject, String target) {
        String normServerUrl = (server.toString().endsWith("/") ?
                server.toString().substring(0, server.toString().length() - 1) :
                server.toString());
        return normServerUrl + urlPattern.replace("{subject}", subject).replace("{target}", target);
    }
}
