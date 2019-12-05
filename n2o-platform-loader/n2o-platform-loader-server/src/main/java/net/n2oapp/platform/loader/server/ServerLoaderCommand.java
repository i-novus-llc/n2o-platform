package net.n2oapp.platform.loader.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.core.io.Resource;

import java.net.URI;

/**
 * Команда для запуска загрузки данных на сервере
 */
public class ServerLoaderCommand {
    @JsonIgnore
    private URI server;
    private String subject;
    private String target;
    @JsonIgnore
    private Resource file;
    @JsonIgnore
    private Class<? extends ServerLoader> loaderClass;

    public ServerLoaderCommand() {
    }

    public ServerLoaderCommand(URI server, String subject, String target, Resource file) {
        this.server = server;
        this.subject = subject;
        this.target = target;
        this.file = file;
    }

    public ServerLoaderCommand(URI server, String subject, String target, Resource file, Class<? extends ServerLoader> loaderClass) {
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

    public Class<? extends ServerLoader> getLoaderClass() {
        return loaderClass;
    }

    public void setLoaderClass(Class<? extends ServerLoader> loaderClass) {
        this.loaderClass = loaderClass;
    }
}
