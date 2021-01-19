package net.n2oapp.platform.jaxrs.seek;

import org.springframework.data.domain.Sort;

import java.util.List;

public interface SeekableCriteria {

    /**
     * Запрашиваемая страница
     */
    RequestedPageEnum getPage();
    void setPage(RequestedPageEnum page);

    /**
     * Требуемый размер страницы
     */
    Integer getSize();
    void setSize(Integer size);

    /**
     * Сортировка элементов (обязательна)
     *
     * Порядок элементов в данном списке важен.
     * Последним элементом должна идти сортировка по уникальному полю
     * (в случае составного ключа его поля должны идти суффиксом в этом списке)
     */
    List<Sort.Order> getOrders();
    void setOrders(List<Sort.Order> orders);

    /**
     * При запросе за следующей страницей -- список значений полей, указанных в {@link #getOrders()} последней записи текущей страницы.
     * При запросе за предыдущей страницей -- список значений полей, указанных в {@link #getOrders()} первой записи текущей страницы.
     * При запросе за первой/последней страницей данный список будет проигнорирован.
     */
    List<SeekPivot> getPivots();
    void setPivots(List<SeekPivot> pivots);

}
