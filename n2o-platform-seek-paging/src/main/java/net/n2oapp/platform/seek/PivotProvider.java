package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

public interface PivotProvider {

    /**
     * @param property Выражение QueryDSL, представляющее собой путь от рутовой сущности репозитория до поля,
     *                 заданного в сортировке {@link net.n2oapp.platform.jaxrs.seek.SeekableCriteria}
     *                 Например для сущности Animal с полем height это будет QAnimal.animal.height
     */
    <T> T min(ComparableExpressionBase<?> property);

    /**
     * @param property Выражение QueryDSL, представляющее собой путь от рутовой сущности репозитория до поля,
     *                 заданного в сортировке {@link net.n2oapp.platform.jaxrs.seek.SeekableCriteria}
     *                 Например для сущности Animal с полем height это будет QAnimal.animal.height
     */
    <T> T max(ComparableExpressionBase<?> property);

}
