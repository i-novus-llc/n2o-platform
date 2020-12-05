package net.n2oapp.platform.jaxrs.seek;

import org.springframework.data.domain.Sort;

import java.util.List;

public interface SeekableCriteria {

    boolean getNext();
    void setNext(boolean next);

    boolean getPrev();
    void setPrev(boolean prev);

    int getSize();
    void setSize(int size);

    List<Sort.Order> getOrders();
    void setOrders(List<Sort.Order> orders);

    List<SeekPivot> getPivots();
    void setPivots(List<SeekPivot> pivots);

    SeekableCriteria copy();

}
