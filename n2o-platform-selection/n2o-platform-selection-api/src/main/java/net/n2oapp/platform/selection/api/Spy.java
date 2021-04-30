package net.n2oapp.platform.selection.api;

/**
 * Реализация этого интерфейса оборачивает {@link Selective} объект типа {@code <T>} и делегирует доступ к его свойствам.<br>
 * Помимо этого она так же следит, чтобы доступ к свойствам объекта был согласован с выборкой типа {@code <S>},
 * то есть все обращения происходили только к проинициализированным свойствам.
 */
public interface Spy<T, S extends Selection<T>> {

    S getSelection();

    T getModel();

}
