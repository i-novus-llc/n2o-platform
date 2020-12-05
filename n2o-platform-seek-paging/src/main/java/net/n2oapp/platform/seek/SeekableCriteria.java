package net.n2oapp.platform.seek;

import org.springframework.data.domain.Sort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.List;

public class SeekableCriteria {

    @QueryParam("next")
    @DefaultValue("true")
    private boolean next = true;

    @QueryParam("prev")
    @DefaultValue("false")
    private boolean prev;

    @QueryParam("size")
    @DefaultValue("10")
    private int size = 10;

    @QueryParam("sort")
    private List<Sort.Order> orders;

    @QueryParam("last")
    private List<SeekPivot> pivots;

    public boolean getNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.prev = !next;
        this.next = next;
    }

    public boolean getPrev() {
        return prev;
    }

    public void setPrev(boolean prev) {
        this.next = !prev;
        this.prev = prev;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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
