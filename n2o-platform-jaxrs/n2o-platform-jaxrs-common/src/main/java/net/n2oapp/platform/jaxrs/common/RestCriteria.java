package net.n2oapp.platform.jaxrs.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Универсальный способ передачи параметров фильтрации, паджинации и сортировки в REST запросе
 */
public class RestCriteria implements Pageable {
    private @QueryParam("page") @DefaultValue("0") int page;
    private @QueryParam("size") @DefaultValue("10") int size;
    private @QueryParam("sort") List<Sort.Order> orders;

    public RestCriteria() {
    }

    public RestCriteria(int page, int size, Sort sort) {
        this(page, size);
        orders = new ArrayList<>();
        sort.forEach(orders::add);
    }

    public RestCriteria(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        } else if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        } else {
            this.page = page;
            this.size = size;
        }
    }

    @Override
    @JsonProperty("size")
    public int getPageSize() {
        return this.size;
    }

    @Override
    @JsonProperty("page")
    public int getPageNumber() {
        return this.page;
    }

    @Override
    @JsonIgnore
    public int getOffset() {
        return this.page * this.size;
    }

    @Override
    @JsonIgnore
    public Sort getSort() {
        if (orders != null && !orders.isEmpty()) {
            return new Sort(orders);
        }
        else
            return null;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @JsonIgnore
    public List<Sort.Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Sort.Order> orders) {
        this.orders = orders;
    }

    @Override
    @JsonIgnore
    public Pageable next() {
        return new RestCriteria(this.getPageNumber() + 1, this.getPageSize(), this.getSort());
    }

    @JsonIgnore
    public Pageable previous() {
        return this.getPageNumber() == 0 ? this : new RestCriteria(this.getPageNumber() - 1, this.getPageSize(), this.getSort());
    }

    @Override
    @JsonIgnore
    public boolean hasPrevious() {
        return this.page > 0;
    }

    @JsonIgnore
    public Pageable previousOrFirst() {
        return this.hasPrevious() ? this.previous() : this.first();
    }

    @Override
    @JsonIgnore
    public Pageable first() {
        return new RestCriteria(0, this.getPageSize(), this.getSort());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestCriteria)) return false;
        RestCriteria that = (RestCriteria) o;
        return page == that.page &&
                size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size);
    }
}
