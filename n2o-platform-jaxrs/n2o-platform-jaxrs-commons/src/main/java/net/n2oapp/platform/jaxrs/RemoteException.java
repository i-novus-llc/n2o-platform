package net.n2oapp.platform.jaxrs;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Исключение со стектрейсом исключения, произошедшего на удаленном REST сервере
 */
public class RemoteException extends RuntimeException {
    private static final Pattern STACKTRACE_ELEMENT_PATTERN = Pattern.compile(".+\\(.+:[0-9]+\\)");
    private static final long serialVersionUID = 4938245199690655790L;

    public RemoteException(String[] stackTrace) {
        super(stackTrace[0]);
        setStackTrace(Arrays.stream(stackTrace).map(RemoteException::parseFrame)
                .filter(Objects::nonNull).toArray(StackTraceElement[]::new));
    }

    private static StackTraceElement parseFrame(String stackTraceFrame) {
        String frame = stackTraceFrame.replace("\tat ", "").trim();
        if (STACKTRACE_ELEMENT_PATTERN.matcher(frame).matches()) {
            String classAndMethod = frame.substring(0, frame.indexOf("("));
            String fileAndLine = frame.substring(frame.indexOf("(") + 1, frame.length() - 1);
            String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
            String methodName = classAndMethod.substring(classAndMethod.lastIndexOf(".") + 1);
            String fileName = fileAndLine.substring(0, fileAndLine.indexOf(":"));
            int lineNumber = Integer.valueOf(fileAndLine.substring(fileAndLine.indexOf(":") + 1));
            return new StackTraceElement(className, methodName, fileName, lineNumber);
        } else {
            return null;
        }
    }
}
