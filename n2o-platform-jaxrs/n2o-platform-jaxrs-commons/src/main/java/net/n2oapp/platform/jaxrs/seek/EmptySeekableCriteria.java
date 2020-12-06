package net.n2oapp.platform.jaxrs.seek;

import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Objects;

public class EmptySeekableCriteria implements SeekableCriteria {

    @NotNull
    @QueryParam("page")
    @DefaultValue("FIRST")
    private RequestedPageEnum page = RequestedPageEnum.FIRST;

    @PositiveOrZero
    @QueryParam("size")
    @DefaultValue("10")
    private Integer size = 10;

    @NotEmpty
    @QueryParam("sort")
    private List<Sort.Order> orders;

    @QueryParam("piv")
    private List<SeekPivot> pivots;

    @Override
    public RequestedPageEnum getPage() {
        return page;
    }

    @Override
    public void setPage(RequestedPageEnum page) {
        this.page = page;
    }

    @Override
    public Integer getSize() {
        return size;
    }

    @Override
    public void setSize(Integer size) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmptySeekableCriteria)) return false;
        EmptySeekableCriteria that = (EmptySeekableCriteria) o;
        return page == that.page && size.equals(that.size) && Objects.equals(orders, that.orders) && Objects.equals(pivots, that.pivots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, orders, pivots);
    }

}
