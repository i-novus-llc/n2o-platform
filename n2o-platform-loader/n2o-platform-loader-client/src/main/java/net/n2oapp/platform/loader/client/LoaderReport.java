package net.n2oapp.platform.loader.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Отчет о выполнении загрузок
 */
public class LoaderReport {
    private List<Fail> fails = new ArrayList<>();
    private List<ClientLoaderCommand> success = new ArrayList<>();
    private List<ClientLoaderCommand> aborted = new ArrayList<>();

    /**
     * Загрузка прошла успешно?
     *
     * @return true Да
     */
    public boolean isSuccess() {
        return fails.isEmpty() && aborted.isEmpty();
    }

    public void addFail(ClientLoaderCommand command, Exception e) {
        fails.add(new Fail(command, e));
    }

    public void addSuccess(ClientLoaderCommand command) {
        success.add(command);
    }

    public void addAborted(List<ClientLoaderCommand> commands) {
        aborted.addAll(commands);
    }

    public List<Fail> getFails() {
        return Collections.unmodifiableList(fails);
    }

    public List<ClientLoaderCommand> getSuccess() {
        return Collections.unmodifiableList(success);
    }

    public List<ClientLoaderCommand> getAborted() {
        return Collections.unmodifiableList(aborted);
    }

    public static class Fail {
        private ClientLoaderCommand command;
        @JsonIgnore
        private Exception exception;

        public Fail(ClientLoaderCommand command, Exception exception) {
            this.command = command;
            this.exception = exception;
        }

        public ClientLoaderCommand getCommand() {
            return command;
        }

        public Exception getException() {
            return exception;
        }

        public String getMessage() {
            return exception.getMessage();
        }
    }
}
