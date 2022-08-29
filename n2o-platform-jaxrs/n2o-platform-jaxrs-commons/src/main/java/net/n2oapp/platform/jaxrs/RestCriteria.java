package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Универсальный способ передачи параметров фильтрации, паджинации и сортировки в REST запросе
 */
public abstract class RestCriteria implements Pageable {

    public static final int FIRST_PAGE_NUMBER = 0;
    public static final int MIN_PAGE_SIZE = 1;

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private @QueryParam("page") @DefaultValue(DEFAULT_PAGE_NUMBER + "") int pageNumber;
    private @QueryParam("size") @DefaultValue(DEFAULT_PAGE_SIZE + "") int pageSize;
    private @QueryParam("sort") List<Sort.Order> orders;

    public RestCriteria() {
        this(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    public RestCriteria(int pageNumber, int pageSize, Sort sort) {
        this(pageNumber, pageSize);
        orders = sort.get().collect(Collectors.toList());
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
        return ((long) this.pageNumber) * this.pageSize;
    }

    @Override
    @JsonIgnore
    public Sort getSort() {
        if (!CollectionUtils.isEmpty(orders)) {
            return Sort.by(orders);
        }
        return Sort.by(getDefaultOrders());
    }

    protected abstract List<Sort.Order> getDefaultOrders();

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

    public void setOrders(List<Sort.Order> orders) {
        this.orders = orders;
    }

    @Override
    @JsonIgnore
    public RestCriteria next() {
        return constructNew(pageNumber + 1, pageSize, orders);
    }

    @JsonIgnore
    public RestCriteria previous() {
        if (pageNumber == FIRST_PAGE_NUMBER)
            throw new IllegalStateException("The beginning reached. Page number is: " + FIRST_PAGE_NUMBER);
        return constructNew(pageNumber - 1, pageSize, orders);
    }

    @Override
    @JsonIgnore
    public boolean hasPrevious() {
        return this.pageNumber > FIRST_PAGE_NUMBER;
    }

    @Override
    @JsonIgnore
    public RestCriteria previousOrFirst() {
        return this.hasPrevious() ? this.previous() : this.first();
    }

    @Override
    @JsonIgnore
    public RestCriteria first() {
        return constructNew(FIRST_PAGE_NUMBER, pageSize, orders);
    }

    @Override
    @JsonIgnore
    public boolean isPaged() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isUnpaged() {
        return !isPaged();
    }

    private RestCriteria constructNew(int pageNumber, int pageSize, List<Sort.Order> orders) {
        Class<? extends RestCriteria> c = this.getClass();
        RestCriteria criteria;
        try {
            criteria = c.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Exception occurred while creating criteria of classs: " + c, e);
        }
        criteria.setPageNumber(pageNumber);
        criteria.setPageSize(pageSize);
        criteria.setOrders(orders);
        Map<String, Object> fields = new HashMap<>();
        ReflectionUtils.doWithFields(c, field -> {
            if (canSetField(field)) {
                field.setAccessible(true);
                Object param = field.get(this);
                fields.put(field.getName().intern(), param);
            }
        });
        ReflectionUtils.doWithFields(c, field -> {
            if (canSetField(field))
                field.set(criteria, fields.get(field.getName()));
        });
        return criteria;
    }

    private boolean canSetField(Field field) {
        return field.getDeclaringClass() != RestCriteria.class && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestCriteria)) return false;
        RestCriteria that = (RestCriteria) o;
        return pageNumber == that.pageNumber &&
                pageSize == that.pageSize &&
                Objects.equals(orders, that.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, pageSize, orders);
    }

    @Override
    public RestCriteria withPage(int pageNumber) {
        return constructNew(pageNumber, pageSize, orders);
    }
}
