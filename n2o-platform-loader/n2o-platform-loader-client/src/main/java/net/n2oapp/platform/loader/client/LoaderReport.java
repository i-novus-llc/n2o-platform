package net.n2oapp.platform.loader.client;

import java.util.ArrayList;
import java.util.List;

public class LoaderReport {
    private List<Fail> fails = new ArrayList<>();
    private List<LoaderCommand> success = new ArrayList<>();
    private List<LoaderCommand> aborted = new ArrayList<>();

    public boolean isSuccess() {
        return fails.isEmpty() && aborted.isEmpty();
    }

    public void addFail(LoaderCommand command, Exception e) {
        fails.add(new Fail(command, e));
    }

    public void addSuccess(LoaderCommand command) {
        success.add(command);
    }

    public void addAborted(List<LoaderCommand> commands) {
        aborted.addAll(commands);
    }

    public List<Fail> getFails() {
        return fails;
    }

    public List<LoaderCommand> getSuccess() {
        return success;
    }

    public List<LoaderCommand> getAborted() {
        return aborted;
    }

    public static class Fail {
        private LoaderCommand command;
        private Exception exception;

        public Fail(LoaderCommand command, Exception exception) {
            this.command = command;
            this.exception = exception;
        }

        public LoaderCommand getCommand() {
            return command;
        }

        public Exception getException() {
            return exception;
        }
    }
}
