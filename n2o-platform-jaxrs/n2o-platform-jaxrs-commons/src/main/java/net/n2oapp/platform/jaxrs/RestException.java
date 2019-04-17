package net.n2oapp.platform.jaxrs;

import java.util.*;

/**
 * Исключение, выбрасываемое на клиенте при возникновении исключения в REST сервисе
 */
public class RestException extends RuntimeException {
    private static final long serialVersionUID = 111344339012373978L;
    private final int responseStatus;
    private final RestMessage restMessage;

    public RestException(RestMessage restMessage, int responseStatus) {
        super(restMessage.getMessage(), getRemoteCause(restMessage));
        this.restMessage = restMessage;
        this.responseStatus = responseStatus;
    }

    public List<RestMessage.Error> getErrors() {
        return restMessage.getErrors();
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    private static RemoteException getRemoteCause(RestMessage restMessage) {
        if (restMessage.getStackTrace() != null) {
            return new RemoteException(restMessage.getStackTrace());
        } else {
            return null;
        }
    }
}
