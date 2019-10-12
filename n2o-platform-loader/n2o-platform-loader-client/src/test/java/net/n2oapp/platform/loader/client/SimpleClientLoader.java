package net.n2oapp.platform.loader.client;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

class SimpleClientLoader implements ClientLoader {

    @Override
    public void load(URI server, String subject, String target, Resource file) {
        String data = null;
        try {
            data = new BufferedReader(new InputStreamReader(file.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(server.toString() + "/simple/" + subject + "/" + target))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<String> response;
        try {
            response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new IllegalStateException("Status code " + response.statusCode());
    }
}
