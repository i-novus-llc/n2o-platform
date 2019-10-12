package net.n2oapp.platform.loader.client;

import org.springframework.core.io.Resource;

import java.net.URI;

/**
 * Команда для запуска загрузки данных на клиенте
 */
public class ClientLoaderCommand {
    private URI server;
    private String subject;
    private String target;
    private Resource file;
    private Class<? extends ClientLoader> loaderClass;

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
}
