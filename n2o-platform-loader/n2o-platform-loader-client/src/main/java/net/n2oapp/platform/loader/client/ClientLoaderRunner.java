package net.n2oapp.platform.loader.client;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Отправка всех данных загрузчиков на сервер
 */
public class ClientLoaderRunner {
    private List<ClientLoader> loaders;
    private List<ClientLoaderCommand> commands = new ArrayList<>();
    private boolean failFast = true;

    public ClientLoaderRunner(List<ClientLoader> loaders) {
        if (loaders == null || loaders.isEmpty())
            throw new IllegalArgumentException("Loaders required");
        this.loaders = loaders;
    }

    /**
     * Добавить информацию о загрузке
     *
     * @param command Информация о загрузке
     */
    public ClientLoaderRunner add(ClientLoaderCommand command) {
        commands.add(command);
        return this;
    }

    /**
     * Добавить информацию о загрузке с классом загрузчика
     *
     * @param serverUrl   Адрес сервера
     * @param subject     Владелец данных
     * @param target      Цель загрузки
     * @param fileUri     Адрес ресурса с данными
     * @param loaderClass Класс клиентского загрузчика
     */
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

    /**
     * Добавить информацию о загрузке
     *
     * @param serverUrl Адрес сервера
     * @param subject   Владелец данных
     * @param target    Цель загрузки
     * @param filePath  Путь к файлу в classpath
     */
    public ClientLoaderRunner add(String serverUrl, String subject, String target, String filePath) {
        return add(serverUrl, subject, target, new ClassPathResource(filePath), null);
    }

    /**
     * Добавить информацию о загрузке с классом загрузчика
     *
     * @param serverUrl   Адрес сервера
     * @param subject     Владелец данных
     * @param target      Цель загрузки
     * @param filePath    Путь к файлу в classpath
     * @param loaderClass Класс клиентского загрузчика
     */
    public ClientLoaderRunner add(String serverUrl, String subject, String target,
                                  String filePath, Class<? extends ClientLoader> loaderClass) {
        return add(serverUrl, subject, target, new ClassPathResource(filePath), loaderClass);
    }

    /**
     * Запуск отправки загрузчиков
     *
     * @return Отчет о выполнении
     */
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

    /**
     * Найти клиентский загрузчик
     *
     * @param loaderClass Класс загрузчика
     * @return Клиентский загрузчик
     * @throws IllegalArgumentException Загрузчик не был найден
     */
    protected ClientLoader find(Class<? extends ClientLoader> loaderClass) {
        if (loaderClass != null) {
            Optional<ClientLoader> loader = loaders.stream()
                    .filter(l -> l.getClass().equals(loaderClass))
                    .findFirst();
            if (loader.isEmpty())
                throw new IllegalArgumentException("Loader bean " + loaderClass + " not found");
            else
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
