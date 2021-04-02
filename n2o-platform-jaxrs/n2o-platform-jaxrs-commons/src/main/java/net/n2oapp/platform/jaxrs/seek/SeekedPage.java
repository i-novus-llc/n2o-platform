package net.n2oapp.platform.jaxrs.seek;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.util.Streamable;

import java.util.List;

@JsonDeserialize(as = SeekedPageImpl.class)
public interface SeekedPage<T> extends Streamable<T> {

    /**
     * @return Содержимое страницы
     */
    List<T> getContent();

    /**
     * Есть ли страницы после этой?
     */
    @JsonProperty
    boolean hasNext();

    /**
     * Есть ли страницы перед этой?
     */
    @JsonProperty
    boolean hasPrev();

    /**
     * @return Кол - во элементов в странице
     */
    @JsonIgnore
    default int size() {
        return getContent().size();
    }

    /**
     * @return Пустая страница
     */
    @SuppressWarnings("unchecked")
    static <T> SeekedPage<T> empty() {
        return (SeekedPage<T>) SeekedPageImpl.EMPTY;
    }


}
