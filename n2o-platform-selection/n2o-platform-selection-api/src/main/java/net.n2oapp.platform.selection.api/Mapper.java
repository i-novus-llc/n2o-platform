package net.n2oapp.platform.selection.api;

/**
 * Основной интерфейс, который знает, как отобразить некую сущность в DTO типа {@code <E>}.
 * Сущностью может выступать что угодно, будь то сущность JPA или самая обычная {@code Map}.
 *
 * @param <E> Тип DTO
 */
public interface Mapper<E> {

    /**
     * @return Пустой DTO, чьи поля будут выборочно отображены в соответствии с {@link Selection<E>}
     */
    E create();

}
