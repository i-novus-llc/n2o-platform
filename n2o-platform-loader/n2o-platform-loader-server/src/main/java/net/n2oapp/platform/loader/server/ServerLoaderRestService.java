package net.n2oapp.platform.loader.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * REST сервис загрузки данных
 */
@Path("/loaders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api("Загрузчики данных")
public interface ServerLoaderRestService extends ServerLoaderRunner {

    @POST
    @Path("/{subject}/{target}")
    @ApiOperation("Загрузить данные")
    @ApiResponse(code = 200, message = "Данные загружены без ошибок")
    @Override
    void run(@ApiParam("Владелец данных") @PathParam("subject") String subject,
             @ApiParam("Вид данных") @PathParam("target") String target,
             InputStream body);

}
