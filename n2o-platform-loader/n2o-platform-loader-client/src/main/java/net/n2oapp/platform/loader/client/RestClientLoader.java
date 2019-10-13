package net.n2oapp.platform.loader.client;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.net.URI;

/**
 * Загрузчик данных через REST API
 * @param <T> Тип данных
 */
public abstract class RestClientLoader<T> implements ClientLoader {
    private RestOperations restTemplate;
    private String endpointPattern = "/load/{subject}/{target}";

    public RestClientLoader(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestClientLoader(RestOperations restTemplate, String endpointPattern) {
        this.restTemplate = restTemplate;
        this.endpointPattern = endpointPattern;
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
            throw new LoadingException("Loading failed status " + response.getStatusCodeValue() + " response " + response.getBody());
    }

    protected MultiValueMap<String, String> getHeaders() {
        return null;
    };

    protected abstract T getData(Resource file);

    protected String getUrl(URI server, String subject, String target) {
        String serverUrl = (server.toString().endsWith("/") ?
                server.toString().substring(0, server.toString().length() - 1) :
                server.toString());
        return serverUrl + endpointPattern.replace("{subject}", subject).replace("{target}", target);
    }
}
