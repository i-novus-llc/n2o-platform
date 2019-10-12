package net.n2oapp.platform.loader.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовый запускатель загрузчиков
 */
public abstract class BaseLoaderRunner implements ServerLoaderRunner, ServerLoaderEndpoint {
    private List<ServerLoaderCommand> commands = new ArrayList<>();

    public ServerLoaderRunner add(ServerLoaderCommand command) {
        commands.add(command);
        return this;
    }

    @Override
    public void run(String subject, String target, InputStream body) {
        ServerLoaderCommand command = find(target);
        Object data = read(body, command);
        execute(subject, command, data);
    }

    /**
     * Найти загрузчик по цели
     *
     * @param target Цель
     * @return Загрузчик
     */
    protected ServerLoaderCommand find(String target) {
        return commands.stream().filter(l -> l.getTarget().equals(target)).findFirst().orElseThrow();
    }

    /**
     * Прочитать данные
     *
     * @param body    Поток данных
     * @param command Команда
     * @return Данные
     */
    protected abstract Object read(InputStream body, ServerLoaderCommand command);

    /**
     * Запуск загрузчика
     *
     * @param subject Владелец данных
     * @param command Команда
     * @param data    Данные
     */
    @SuppressWarnings("unchecked")
    protected void execute(String subject, ServerLoaderCommand command, Object data) {
        ServerLoader<?> loader = command.getLoader();
        ((ServerLoader<Object>) loader).load(data, subject);
    }

    public List<ServerLoaderCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
