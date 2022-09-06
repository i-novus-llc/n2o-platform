package net.n2oapp.platform.loader.client;

import org.springframework.core.io.Resource;

import java.io.*;
import java.net.*;
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
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(server.toString() + "/simple/" + subject + "/" + target).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty( "Content-Length", Integer.toString(data.length()));
            connection.setRequestMethod("POST");
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(data.getBytes());
            }
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300)
                throw new IllegalStateException("Status code " + responseCode);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
