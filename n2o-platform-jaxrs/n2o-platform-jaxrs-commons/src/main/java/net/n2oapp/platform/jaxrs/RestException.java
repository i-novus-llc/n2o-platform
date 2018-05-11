package net.n2oapp.platform.jaxrs;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Исключение, выбрасываемое на клиенте при возникновении исключения в REST сервисе
 */
public class RestException extends RuntimeException {
    private static final long serialVersionUID = 111344339012373978L;
    private static final Pattern STACKTRACE_ELEMENT_PATTERN = Pattern.compile(".+\\(.+:[0-9]+\\)");

    private RestMessage restMessage;

    public RestException(RestMessage restMessage) {
        super(restMessage.getMessage());
        this.restMessage = restMessage;
    }

    public RestException(RestMessage restMessage, Throwable cause) {
        super(restMessage.getMessage(), cause);
        this.restMessage = restMessage;
    }

    public List<RestMessage.Error> getErrors() {
        return restMessage.getErrors();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        if (restMessage.getStackTrace() != null) {
            List<StackTraceElement> stackTraceElements = new ArrayList<>(Arrays.asList(super.getStackTrace()));
            stackTraceElements.addAll(Arrays.stream(restMessage.getStackTrace()).map(this::parseFrame)
                    .filter(Objects::nonNull).collect(Collectors.toList()));
            return stackTraceElements.toArray(new StackTraceElement[stackTraceElements.size()]);
        } else {
            return super.getStackTrace();
        }
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (restMessage.getStackTrace() != null) {
            writer.print("Caused by: ");
            Arrays.stream(restMessage.getStackTrace()).forEach(writer::println);
        }
    }

    private StackTraceElement parseFrame(String stackTraceFrame) {
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
