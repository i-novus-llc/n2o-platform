package net.n2oapp.platform.jaxrs.seek;

import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.Objects;

/**
 * Базовая реализация интерфейса {@link Seekable}
 */
public class SeekRequest implements Seekable {

    @NotNull
    @QueryParam("page")
    @DefaultValue("FIRST")
    private RequestedPageEnum page = RequestedPageEnum.FIRST;

    @PositiveOrZero
    @QueryParam("size")
    @DefaultValue("10")
    private Integer size = 10;

    @NotNull
    @QueryParam("sort")
    private Sort sort;

    @QueryParam("piv")
    private List<SeekPivot> pivots;

    @Override
    public RequestedPageEnum getPage() {
        return page;
    }

    public void setPage(RequestedPageEnum page) {
        this.page = page;
    }

    @Override
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    @Override
    public List<SeekPivot> getPivots() {
        return pivots;
    }

    public void setPivots(List<SeekPivot> pivots) {
        this.pivots = pivots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeekRequest)) return false;
        SeekRequest that = (SeekRequest) o;
        return page == that.page && size.equals(that.size) && Objects.equals(sort, that.sort) && Objects.equals(pivots, that.pivots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sort, pivots);
    }

}
