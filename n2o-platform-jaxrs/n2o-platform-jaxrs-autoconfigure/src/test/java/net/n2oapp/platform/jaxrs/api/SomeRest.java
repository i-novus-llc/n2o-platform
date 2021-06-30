package net.n2oapp.platform.jaxrs.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.n2oapp.platform.jaxrs.seek.SeekRequest;
import net.n2oapp.platform.jaxrs.seek.SeekedPage;
import org.springframework.data.domain.Page;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST сервис для демонстрации возможностей библиотеки
 */
@Path("/example")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Api("REST сервис для демонстрации возможностей библиотеки")
public interface SomeRest {

    @GET
    @Path("/search")
    @ApiOperation("Найти страницу моделей по критериям поиска")
    @ApiResponse(code = 200, message = "Страница моделей")
    Page<SomeModel> search(@BeanParam SomeCriteria criteria);

    @GET
    @Path("/search/model")
    @ApiOperation("Поиск абстрактной модели")
    @ApiResponse(code = 200, message = "Страница моделей")
    Page<AbstractModel> searchModel(@BeanParam SomeCriteria criteria);

    @GET
    @Path("/list")
    @ApiOperation("Найти список моделей по критериям поиска (без подсчета общего количества)")
    @ApiResponse(code = 200, message = "Список моделей")
    List<SomeModel> searchWithoutTotalElements(@BeanParam SomeCriteria criteria);

    @GET
    @Path("/count")
    @ApiOperation("Подсчитать количество записей по критериям поиска")
    @ApiResponse(code = 200, message = "Количество записей")
    Long count(@BeanParam SomeCriteria criteria);

    @GET
    @Path("/{id}")
    @ApiOperation("Получить запись по идентификатору")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Модель найденной записи"),
            @ApiResponse(code = 404, message = "Ошибка о том, что запись не найдена")})
    SomeModel getById(@PathParam("id") Long id);

    @POST
    @Path("/")
    @ApiOperation("Создать запись")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Идентификатор созданной записи"),
            @ApiResponse(code = 400, message = "Ошибка с сообщением валидации")})
    Long create(@Valid SomeModel model);

    @PUT
    @Path("/")
    @ApiOperation("Изменить запись")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Запись изменена"),
            @ApiResponse(code = 400, message = "Ошибка с сообщением валидации"),
            @ApiResponse(code = 500, message = "Ошибка с сообщением об ошибке и стектрейсом")})
    void update(SomeModel model);

    @DELETE
    @Path("/{id}")
    @ApiOperation("Удалить запись")
    @ApiResponse(code = 200, message = "Запись удалена")
    void delete(@PathParam("id") Long id);

    @POST
    @Path("/multipleErrors")
    @ApiOperation("Список ошибок")
    void throwErrors();

    @GET
    @Path("/timeoutSuccess")
    String timeoutSuccess() throws InterruptedException;

    @GET
    @Path("/timeoutFailure")
    String timeoutFailure() throws InterruptedException;

    @GET
    @Path("/searchBySetOfTypedList")
    @ApiOperation("Поиск по списку")
    @ApiResponse(code = 200, message = "Список")
    List<LocalDateTime> searchBySetOfTypedList(@QueryParam("setOfList") Set<List<LocalDateTime>> setOfList);

    @GET
    @Path("/searchBySetOfMap")
    @ApiOperation("Поиск по карте")
    @ApiResponse(code = 200, message = "Карта")
    Map<String,String> searchBySetOfTypedMap(@QueryParam("map") Map<String, String> setOfMap);

    @GET
    @Path("/authHeader")
    @ApiOperation("Вернуть заголовок Authorization")
    @ApiResponse(code = 200, message = "Страница моделей")
    Map<String, String> authHeader();

    @GET
    @Path("/listOfAbstractModels")
    List<AbstractModel<?>> getListOfAbstractModels();

    @GET
    @Path("/genericList")
    List<ListModel> getListModels();

    @GET
    @Path("/seek")
    SeekedPage<String> searchSeeking(@BeanParam @Valid SeekRequest request);

}
