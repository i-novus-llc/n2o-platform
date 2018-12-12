package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

/**
 * Универсальный способ передачи параметров фильтрации, паджинации и сортировки в REST запросе
 */
public class RestCriteria implements Pageable {

    public static final int FIRST_PAGE_NUMBER = 0;
    public static final int MIN_PAGE_SIZE = 1;

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
        this.setPageNumber(pageNumber);
        this.setPageSize(pageSize);
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
    public long getOffset() {
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
        if (pageNumber < FIRST_PAGE_NUMBER) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        } else {
            this.pageNumber = pageNumber;
        }
    }

    public void setPageSize(int pageSize) {
        if (pageSize < MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        } else {
            this.pageSize = pageSize;
        }
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
        return this.getPageNumber() == FIRST_PAGE_NUMBER ? this : new RestCriteria(this.getPageNumber() - 1, this.getPageSize(), this.getSort());
    }

    @Override
    @JsonIgnore
    public boolean hasPrevious() {
        return this.pageNumber > FIRST_PAGE_NUMBER;
    }

    @JsonIgnore
    public Pageable previousOrFirst() {
        return this.hasPrevious() ? this.previous() : this.first();
    }

    @Override
    @JsonIgnore
    public Pageable first() {
        return new RestCriteria(FIRST_PAGE_NUMBER, this.getPageSize(), this.getSort());
    }

    @JsonIgnore
    static Pageable unpaged() {
        return Pageable.unpaged();
    }

    @JsonIgnore
    public boolean isPaged() {
        return true;
    }

    @JsonIgnore
    public boolean isUnpaged() {
        return !isPaged();
    }
}
