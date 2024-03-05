package net.n2oapp.platform.selection.integration.rest;

import net.n2oapp.platform.selection.integration.model.Employee;
import org.springframework.data.domain.Page;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/selective")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface SelectiveRest {

    @GET
    @Path("/search")
    Page<Employee> search(@BeanParam EmployeeCriteria criteria);

}
