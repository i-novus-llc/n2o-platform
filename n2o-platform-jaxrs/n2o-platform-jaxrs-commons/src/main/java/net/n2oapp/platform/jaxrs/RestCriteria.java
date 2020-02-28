package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
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

    @JsonIgnore
    public List<Sort.Order> getOrders() {
        return orders;
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
        Class<?>[] argsType = {Integer.TYPE, Integer.TYPE};
        Constructor<? extends RestCriteria> constructor;
        try {
            constructor = c.getConstructor(argsType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Can't access constructor of criteria class: " + c + ". No public constructor with args signature " + Arrays.toString(argsType) + " found.", e);
        }
        RestCriteria criteria;
        try {
            criteria = constructor.newInstance(pageNumber, pageSize);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Can't instantiate criteria of class: " + c, e);
        }
        criteria.setOrders(orders);
        Map<String, Object> fields = new HashMap<>();
        ReflectionUtils.doWithLocalFields(c, field -> {
            field.setAccessible(true);
            Object param = field.get(this);
            fields.put(field.getName().intern(), param);
        });
        ReflectionUtils.doWithLocalFields(c, field -> field.set(criteria, fields.get(field.getName())));
        return criteria;
    }

}
