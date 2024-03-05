package net.n2oapp.platform.jaxrs.api;

import net.n2oapp.platform.jaxrs.RestCriteria;
import org.springframework.data.domain.Sort;

import jakarta.ws.rs.QueryParam;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static java.util.Collections.singletonList;

public class SomeCriteria extends RestCriteria {

    @QueryParam("name")
    private String likeName;
    @QueryParam("date")
    private Date dateBegin;
    @QueryParam("dateEnd")
    private LocalDateTime dateEnd;

    public SomeCriteria() {
    }

    public SomeCriteria(int page, int size) {
        super(page, size);
    }

    @Override
    protected List<Sort.Order> getDefaultOrders() {
        return singletonList(Sort.Order.asc("date"));
    }

    public SomeCriteria(int page, int size, Sort sort) {
        super(page, size, sort);
    }

    public String getLikeName() {
        return this.likeName;
    }

    public Date getDateBegin() {
        return this.dateBegin;
    }

    public LocalDateTime getDateEnd() {
        return this.dateEnd;
    }

    public void setLikeName(String likeName) {
        this.likeName = likeName;
    }

    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }
}
