package net.n2oapp.platform.loader.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * REST сервис загрузки данных
 */
@Path("/loaders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Загрузчики данных")
public interface ServerLoaderRestService extends ServerLoaderRunner {

    @POST
    @Path("/{subject}/{target}")
    @Operation(summary = "Загрузить данные",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Данные загружены без ошибок",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON))
            })
    @Override
    void run(@Parameter(name = "Владелец данных") @PathParam("subject") String subject,
             @Parameter(name = "Вид данных") @PathParam("target") String target,
             @RequestBody(description = "Данные", required = true,
                     content = @Content(schema = @Schema(implementation = InputStream.class))) InputStream body);
}
