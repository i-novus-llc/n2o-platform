package net.n2oapp.platform.selection.api;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * <pre>
 * Основной интерфейс, определяющий какие именно поля будут отображены {@link Mapper<E>}-ом
 *
 * Методы, которые определяют выборку полей типа {@code E} должны быть помечены аннотацией {@link SelectionKey}
 * и могут быть двух видов:
 *
 * 1) Метод возвращающий {@link SelectionEnum}. Таким методам обычно соответствуют примитивные поля сущности (числа, даты, строки и т.д).
 *
 * 2) А так же парные методы:
 *      а) Метод возвращающий {@link SelectionEnum}.
 *      б) И связанный с ним через {@code selectionKey} метод, возвращающий вложенный {@code Selection<X>}
 *
 * Аннотацией {@link SelectionKey} могут быть так же помечены поля типа {@link SelectionEnum}.
 * В таком случае у него должен быть стандартный геттер.
 *
 * </pre>
 * @param <E> Тип DTO для этой выборки
 */
public interface Selection<E> {

    /**
     * Метод-маркер для вывода типа {@code <E>} в runtime
     */
    default E typeMarker() {
        return null;
    }

    /**
     * @see SelectionPropagationEnum
     */
    default SelectionPropagationEnum propagation() {
        return SelectionPropagationEnum.NORMAL;
    }

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
        if (encodedJson == null || encodedJson.isBlank())
            return null;
        try {
            return Util.MAPPER.readValue(Util.decode(encodedJson), target);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
