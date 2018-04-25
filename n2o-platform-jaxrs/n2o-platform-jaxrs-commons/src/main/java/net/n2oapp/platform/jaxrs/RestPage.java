package net.n2oapp.platform.jaxrs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Страница данных Spring Data серилизуемая через Jackson ObjectMapper
 */
public class RestPage<T> extends PageImpl<T>{
    private long totalElements;
    private List<T> content;
    private Sort sort;
    @JsonIgnore
    private int number;
    @JsonIgnore
    private int numberOfElements;
    @JsonIgnore
    private int size;
    @JsonIgnore
    private int totalPages;
    @JsonIgnore
    private boolean first;
    @JsonIgnore
    private boolean last;

    public RestPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
        this.content = content;
        this.totalElements = total;
        this.sort = pageable.getSort();
    }

    public RestPage(List<T> content) {
        super(content);
    }

    public RestPage() {
        super(new ArrayList<T>());
    }

    public PageImpl<T> unwrap() {
        return new PageImpl<T>(getContent(), new PageRequest(getNumber(),
                getSize(), getSort()), getTotalElements());
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    @Override
    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    @Override
    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    @Override
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
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
    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    @Override
    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}