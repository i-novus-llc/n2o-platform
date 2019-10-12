package net.n2oapp.platform.loader.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Отчет о выполнении загрузок
 */
public class LoaderReport {
    private List<Fail> fails = new ArrayList<>();
    private List<ClientLoaderCommand> success = new ArrayList<>();
    private List<ClientLoaderCommand> aborted = new ArrayList<>();

    /**
     * Загрузка прощла успешно?
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
        return fails;
    }

    public List<ClientLoaderCommand> getSuccess() {
        return success;
    }

    public List<ClientLoaderCommand> getAborted() {
        return aborted;
    }

    public static class Fail {
        private ClientLoaderCommand command;
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
    }
}
