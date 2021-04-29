package net.n2oapp.platform.selection.api;

import org.springframework.lang.NonNull;

/**
 * Основной интерфейс, который знает, как отобразить сущность в модель (DTO) типа {@code <T>}.
 * Сущностью может выступать что угодно, будь то сущность JPA или самая обычная {@code Map}.
 *
 * @param <T> Тип модели (DTO)
 * @param <S> Тип выборки ({@link Selection})
 * @param <E> Тип отображаемой сущности
 */
public interface Fetcher<T, S extends Selection<T>, E> {

    /**
     * @return Пустая модель, чьи поля будут выборочно отображены в соответствии с {@link Selection<T>}
     */
    @NonNull
    T create();

    /**
     * @return Отображаемая сущность
     */
    @NonNull
    E getUnderlyingEntity();

    /**
     *
     * @param selection Выборка
     * @return Проекция модели {@code T} в соответствии с выборкой
     */
    default T resolve(S selection) {
        if (selection == null)
            return null;
        return resolve(selection, selection.propagation());
    }

    /**
     * Данный метод не должен использоваться напрямую.
     * Вместо него следует использовать {@link #resolve(Selection)}
     */
    T resolve(S selection, SelectionPropagation propagation);

}
