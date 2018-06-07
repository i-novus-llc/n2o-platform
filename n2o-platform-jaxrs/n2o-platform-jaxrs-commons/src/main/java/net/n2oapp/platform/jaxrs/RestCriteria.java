package net.n2oapp.platform.jaxrs;

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
    private @QueryParam("page") @DefaultValue("0") int pageNumber;
    private @QueryParam("size") @DefaultValue("10") int pageSize;
    private @QueryParam("sort") List<Sort.Order> orders;

    public RestCriteria() {
    }

    public RestCriteria(int pageNumber, int pageSize, Sort sort) {
        this(pageNumber, pageSize);
        orders = new ArrayList<>();
        sort.forEach(orders::add);
    }

    public RestCriteria(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        } else if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        } else {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }
    }

    @Override
    @JsonProperty("size")
    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    @JsonProperty("page")
    public int getPageNumber() {
        return this.pageNumber;
    }

    @Override
    @JsonIgnore
    public int getOffset() {
        return this.pageNumber * this.pageSize;
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

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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
        return this.pageNumber > 0;
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
        return pageNumber == that.pageNumber && pageSize == that.pageSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, pageSize);
    }
}
