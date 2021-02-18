package net.n2oapp.platform.selection.core.rest;

import net.n2oapp.platform.selection.core.domain.Employee;
import org.springframework.data.domain.Page;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/selective")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface SelectiveRest {

    @GET
    @Path("/search")
    Page<Employee> search(@BeanParam EmployeeCriteria criteria);

}
