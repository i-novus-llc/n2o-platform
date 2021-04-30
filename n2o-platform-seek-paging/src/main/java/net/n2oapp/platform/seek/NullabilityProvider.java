package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

/**
 * Для каждого из свойств определяет, может ли оно принимать значения {@code null}.
 * Использование данного интерфейса позволяет немного ускорить SQL запросы, так как чем больше в таблице {@code nullable} колонок,
 * тем более сложные WHERE-условия для seek-пагинации.
 */
public interface NullabilityProvider {

    /**
     * @param property QueryDSL выражение, представляющее собой путь от рутовой сущности репозитория до поля,
     *                 указанного в сортировке (например для сущности Animal и поля name это QAnimal.animal.name).
     * @return Может ли {@code property} принимать значения {@code null}
     */
    boolean nullable(ComparableExpressionBase<?> property);

}
