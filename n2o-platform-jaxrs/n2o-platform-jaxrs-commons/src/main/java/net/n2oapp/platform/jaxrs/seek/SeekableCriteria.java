package net.n2oapp.platform.jaxrs.seek;

import org.springframework.data.domain.Sort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.List;

public class SeekableCriteria {

    @QueryParam("next")
    @DefaultValue("true")
    private Boolean next;

    @QueryParam("prev")
    @DefaultValue("false")
    private Boolean prev;

    @QueryParam("sort")
    private List<Sort.Order> orders;

    @QueryParam("last")
    private List<SeekPivot> pivots;

    public Boolean getNext() {
        return next;
    }

    public void setNext(Boolean next) {
        this.next = next;
    }

    public Boolean getPrev() {
        return prev;
    }

    public void setPrev(Boolean prev) {
        this.prev = prev;
    }

    public List<Sort.Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Sort.Order> orders) {
        this.orders = orders;
    }

    public List<SeekPivot> getPivots() {
        return pivots;
    }

    public void setPivots(List<SeekPivot> pivots) {
        this.pivots = pivots;
    }
    
}
