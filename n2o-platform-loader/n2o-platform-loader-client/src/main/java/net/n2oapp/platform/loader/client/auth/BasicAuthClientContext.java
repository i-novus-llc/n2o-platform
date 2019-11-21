package net.n2oapp.platform.loader.client.auth;

import org.springframework.util.Assert;

import java.util.Base64;

/**
 * Контекст Basic authorization клиента
 */
public class BasicAuthClientContext implements ClientContext {

    private String username;
    private String password;

    public BasicAuthClientContext(String username, String password) {
        Assert.notNull(username, "Username mustn't be null");
        Assert.notNull(password, "Password mustn't be null");
        this.username = username;
        this.password = password;
    }

    @Override
    public String getAccessToken() {
        return Base64.getEncoder().encodeToString((String.format("%s:%s", username, password).getBytes()));
    }

    @Override
    public String getTokenType() {
        return "Basic";
    }
}
