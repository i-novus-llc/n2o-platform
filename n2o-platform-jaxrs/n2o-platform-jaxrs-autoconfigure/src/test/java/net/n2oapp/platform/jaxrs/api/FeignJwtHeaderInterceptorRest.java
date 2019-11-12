package net.n2oapp.platform.jaxrs.api;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST сервис для теста FeignJwtHeaderInterceptor
 */
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FeignJwtHeaderInterceptorRest {

    @GET
    @Path("/testJwtTokenHeaderInterceptor")
    String testJwtTokenHeaderInterceptor() throws JsonProcessingException;
}
