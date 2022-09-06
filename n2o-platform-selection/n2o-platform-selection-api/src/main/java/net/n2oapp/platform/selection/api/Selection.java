package net.n2oapp.platform.selection.api;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Основной интерфейс, определяющий какие именно поля будут выбраны {@link Fetcher<T>}-ом
 * @param <T> Тип DTO для этой выборки
 */
public interface Selection<T> {

    /**
     * @see SelectionPropagation
     */
    default SelectionPropagation propagation() {
        return SelectionPropagation.NORMAL;
    }

    /**
     * @return {@code true}, если данная выборка является пустой
     * (
     *  то есть
     *      {@link #propagation()} == {@link SelectionPropagation#NORMAL} (или null) и
     *      ни одно значение {@link SelectionEnum} не равно {@link SelectionEnum#T} и
     *      все вложенные выборки так же являются {@code empty()}
     * )
     */
    boolean empty();

    /**
     * Данный метод нужен, чтобы уменьшить кол-во символов, необходимых для передачи выборки в формате JSON,
     * через параметры запроса URL.
     *
     * @param selection Выборка
     * @return Закодированный специальным образом JSON, который не будет закодирован процентами (url-encoded).
     */
    static String encode(Selection<?> selection) {
        if (selection == null)
            return null;
        try {
            String json = Util.MAPPER.writeValueAsString(selection);
            return Util.encode(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Данный метод может работать и не с кодированной через {@link Selection#encode(Selection)} выборкой
     * (то есть он может работать и с обычным JSON).
     *
     * @param encodedJson Выборка в обыкновенном формате JSON, или закодированном через {@link Selection#encode(Selection)}
     * @param target Тип выборки
     * @return Выборка типа {@code <S>}
     */
    static <S extends Selection<?>> S decode(String encodedJson, Class<S> target) {
        if (encodedJson == null || "".equals(encodedJson.trim()))
            return null;
        try {
            return Util.MAPPER.readValue(Util.decode(encodedJson), target);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
