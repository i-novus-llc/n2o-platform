package net.n2oapp.platform.loader.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.*;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.client.RestOperations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Загрузчик json данных
 */
public class JsonClientLoader extends RestClientLoader<String> {

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";

    @Autowired
    private ConfigurableEnvironment environment;

    public JsonClientLoader(RestOperations restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getData(Resource file) {
        if (!getExtension(file.getFilename()).filter("json"::equalsIgnoreCase).isPresent())
            throw new IllegalArgumentException("File " + file.getFilename() + " not a json");
        String data;
        try {
            data = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return resolvePlaceholders(data);
    }

    @Override
    protected MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        return headers;
    }

    private Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf('.') + 1));
    }

    private String resolvePlaceholders(String data) {
        Properties allProperties = new Properties();
        environment.getPropertySources()
                .stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .forEach(propertyName -> allProperties.setProperty(propertyName, environment.getProperty(propertyName)));
        return new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX).replacePlaceholders(data, allProperties);
    }
}