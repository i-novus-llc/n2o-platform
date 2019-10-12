package net.n2oapp.platform.loader.client;

import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonClientLoader extends RestClientLoader<String> {

    public JsonClientLoader(RestOperations restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getData(Resource file) {
        if (getExtension(file.getFilename()).filter(ext -> ext.equalsIgnoreCase("json")).isEmpty())
            throw new IllegalArgumentException("File " + file.getFilename() + " not a json");
        String data;
        try {
            data = new BufferedReader(new InputStreamReader(file.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return data;
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
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
}
