package net.n2oapp.platform.loader.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.core.io.Resource;

import java.net.URI;

/**
 * Команда для запуска загрузки данных на клиенте
 */
public class ClientLoaderCommand {
    @JsonIgnore
    private URI server;
    private String subject;
    private String target;
    @JsonIgnore
    private Resource file;
    @JsonIgnore
    private Class<? extends ClientLoader> loaderClass;
    @JsonIgnore
    private AuthDetails auth;

    public ClientLoaderCommand() {
    }

    public ClientLoaderCommand(URI server, String subject, String target, Resource file) {
        this.server = server;
        this.subject = subject;
        this.target = target;
        this.file = file;
    }

    public ClientLoaderCommand(URI server, String subject, String target, Resource file, Class<? extends ClientLoader> loaderClass) {
        this.server = server;
        this.subject = subject;
        this.target = target;
        this.file = file;
        this.loaderClass = loaderClass;
    }

    public String getServerUri() {
        return server.toString();
    }

    public String getFilename() {
        return file.getFilename();
    }

    public URI getServer() {
        return server;
    }

    public String getSubject() {
        return subject;
    }

    public String getTarget() {
        return target;
    }

    public Resource getFile() {
        return file;
    }

    public void setServer(URI server) {
        this.server = server;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setFile(Resource file) {
        this.file = file;
    }

    public Class<? extends ClientLoader> getLoaderClass() {
        return loaderClass;
    }

    public void setLoaderClass(Class<? extends ClientLoader> loaderClass) {
        this.loaderClass = loaderClass;
    }

    public AuthDetails getAuth() {
        return auth;
    }

    public void setAuth(AuthDetails auth) {
        this.auth = auth;
    }

    public static class AuthDetails {
        private String type;
        private String username;
        private String password;
        private String tokenUri;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.type = "basic";
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getClientId() {
            return getUsername();
        }

        public void setClientId(String clientId) {
            this.type = "oauth2";
            this.username = clientId;
        }

        public String getClientSecret() {
            return getPassword();
        }

        public void setClientSecret(String clientSecret) {
            setPassword(clientSecret);
        }

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }
    }
}
