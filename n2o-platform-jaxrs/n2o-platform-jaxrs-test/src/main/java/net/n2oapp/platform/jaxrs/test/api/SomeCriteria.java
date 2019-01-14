package net.n2oapp.platform.jaxrs.test.api;

import lombok.Getter;
import lombok.Setter;
import net.n2oapp.platform.jaxrs.RestCriteria;
import org.springframework.data.domain.Sort;

import javax.ws.rs.QueryParam;
import java.util.Date;

@Getter @Setter
public class SomeCriteria extends RestCriteria {
    @QueryParam("name")
    private String likeName;
    @QueryParam("date")
    private Date dateBegin;

    public SomeCriteria() {
    }

    public SomeCriteria(int page, int size) {
        super(page, size);
    }

    public SomeCriteria(int page, int size, Sort sort) {
        super(page, size, sort);
    }
}
