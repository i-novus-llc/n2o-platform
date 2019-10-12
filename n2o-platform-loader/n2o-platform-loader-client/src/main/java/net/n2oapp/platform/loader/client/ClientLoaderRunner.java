package net.n2oapp.platform.loader.client;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClientLoaderRunner {
    private List<ClientLoader> loaders;
    private List<ClientLoaderCommand> commands = new ArrayList<>();
    private boolean failFast = true;

    public ClientLoaderRunner(List<ClientLoader> loaders) {
        if (loaders == null || loaders.size() == 0)
            throw new IllegalArgumentException("Loaders required");
        this.loaders = loaders;
    }

    public ClientLoaderRunner add(ClientLoaderCommand command) {
        commands.add(command);
        return this;
    }

    public ClientLoaderRunner add(String serverUrl, String subject, String target,
                                  Resource fileUri, Class<? extends ClientLoader> loaderClass) {
        try {
            commands.add(new ClientLoaderCommand(
                    new URI(serverUrl),
                    subject,
                    target,
                    fileUri,
                    loaderClass));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    public ClientLoaderRunner add(String serverUrl, String subject, String target, String filePath) {
        return add(serverUrl, subject, target, new ClassPathResource(filePath), null);
    }

    public ClientLoaderRunner add(String serverUrl, String subject, String target,
                                  String filePath, Class<? extends ClientLoader> loaderClass) {
        return add(serverUrl, subject, target, new ClassPathResource(filePath), loaderClass);
    }

    public LoaderReport run() {
        LoaderReport report = new LoaderReport();
        for (ClientLoaderCommand command : commands) {
            try {
                find(command.getLoaderClass()).load(
                        command.getServer(),
                        command.getSubject(),
                        command.getTarget(),
                        command.getFile());
                report.addSuccess(command);
            } catch (Exception e) {
                report.addFail(command, e);
                if (failFast) {
                    int i = commands.indexOf(command);
                    if (i < commands.size() - 1)
                        report.addAborted(commands.subList(i + 1, commands.size()));
                    break;
                }
            }
        }
        return report;
    }

    protected ClientLoader find(Class<? extends ClientLoader> loaderClass) {
        if (loaderClass != null) {
            Optional<ClientLoader> loader = loaders.stream()
                    .filter(l -> l.getClass().equals(loaderClass))
                    .findFirst();
            if (loader.isEmpty())
                throw new IllegalArgumentException("Loader bean " + loaderClass + " not found");
            return loader.get();
        } else {
            return loaders.get(0);
        }
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public List<ClientLoader> getLoaders() {
        return Collections.unmodifiableList(loaders);
    }

    public List<ClientLoaderCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
