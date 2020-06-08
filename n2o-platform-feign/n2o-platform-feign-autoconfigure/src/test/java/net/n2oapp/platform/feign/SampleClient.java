package net.n2oapp.platform.feign;

import net.n2oapp.platform.jaxrs.api.AbstractModel;
import net.n2oapp.platform.jaxrs.api.SomeCriteria;
import net.n2oapp.platform.jaxrs.api.SomeModel;
import net.n2oapp.platform.jaxrs.api.SomeRest;
import org.springframework.data.domain.Page;

import javax.validation.Valid;
import javax.ws.rs.*;

/**
 * Приходится дублировать интерфейс {@link SomeRest},
 * т.к. бин, созданный по тому же интерфейсу конфликтует
 * с серверным {@link net.n2oapp.platform.jaxrs.impl.SomeRestImpl}
 */
//@Path("/example")
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
//@FeignClient(name = "${feign.name}", url = "${feign.url}")
public interface SampleClient {
    @GET
    @Path("/search")
    Page<SomeModel> search(@BeanParam SomeCriteria criteria);

    @POST
    @Path("/")
    Long create(@Valid SomeModel model);

    @PUT
    @Path("/")
    void update(SomeModel model);

    @GET
    @Path("/search/model")
    Page<AbstractModel> searchModel(@BeanParam SomeCriteria criteria);

    @POST
    @Path("/multipleErrors")
    void throwErrors();
}
