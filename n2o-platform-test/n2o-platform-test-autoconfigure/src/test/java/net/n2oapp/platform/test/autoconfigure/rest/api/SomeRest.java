package net.n2oapp.platform.test.autoconfigure.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST сервис для тестирования
 */
@Path("/example")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SomeRest {

    @GET
    @Path("/echo")
    String echo();

    @GET
    @Path("/timeoutSuccess")
    String timeoutSuccess() throws InterruptedException;

    @GET
    @Path("/timeoutFailure")
    String timeoutFailure() throws InterruptedException;

}
