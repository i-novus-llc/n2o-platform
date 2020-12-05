package net.n2oapp.platform.jaxrs.seek;

import org.springframework.data.domain.Sort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class EmptySeekableCriteria implements SeekableCriteria {

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

    @QueryParam("piv")
    private List<SeekPivot> pivots;

    @Override
    public boolean getNext() {
        return next;
    }

    @Override
    public void setNext(boolean next) {
        this.prev = !next;
        this.next = next;
    }

    @Override
    public boolean getPrev() {
        return prev;
    }

    @Override
    public void setPrev(boolean prev) {
        this.next = !prev;
        this.prev = prev;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public List<Sort.Order> getOrders() {
        return orders;
    }

    @Override
    public void setOrders(List<Sort.Order> orders) {
        this.orders = orders;
    }

    @Override
    public List<SeekPivot> getPivots() {
        return pivots;
    }

    @Override
    public void setPivots(List<SeekPivot> pivots) {
        this.pivots = pivots;
    }

    @Override
    public SeekableCriteria copy() {
        EmptySeekableCriteria copy = new EmptySeekableCriteria();
        copy.setSize(size);
        copy.setNext(next);
        copy.setPrev(prev);
        copy.setOrders(getOrders().stream().map(o -> new Sort.Order(o.getDirection(), o.getProperty(), o.getNullHandling())).collect(toList()));
        copy.setPivots(getPivots().stream().map(SeekPivot::copy).collect(toList()));
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmptySeekableCriteria)) return false;
        EmptySeekableCriteria that = (EmptySeekableCriteria) o;
        return next == that.next && prev == that.prev && size == that.size && Objects.equals(orders, that.orders) && Objects.equals(pivots, that.pivots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(next, prev, size, orders, pivots);
    }

}
