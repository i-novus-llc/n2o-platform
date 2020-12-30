package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

public interface PivotProvider {

    /**
     * @param property  Выражение QueryDSL, представляющее собой путь от рутовой сущности репозитория до поля,
     *                  заданного в сортировке {@link net.n2oapp.platform.jaxrs.seek.SeekableCriteria}
     *                  Например для сущности Animal с полем height это будет QAnimal.animal.height
     * @return
     *                  Выражение QueryDSL, представляющее собой минимальное значение в контексте {@code property}.
     *                  Это может быть простой скаляр или что — то вроде:<br>
     *                  {@code Expressions.asComparable(JPAExpressions.select(QAnimal.animal.height.min).from(QAnimal.animal))}
     */
    ComparableExpressionBase<?> min(ComparableExpressionBase<?> property);

    /**
     * @param property  Выражение QueryDSL, представляющее собой путь от рутовой сущности репозитория до поля,
     *                  заданного в сортировке {@link net.n2oapp.platform.jaxrs.seek.SeekableCriteria}
     *                  Например для сущности Animal с полем height это будет QAnimal.animal.height
     * @return
     *                  Выражение QueryDSL, представляющее собой минимальное значение в контексте {@code property}.
     *                  Это может быть простой скаляр или что — то вроде:<br>
     *                  {@code Expressions.asComparable(JPAExpressions.select(QAnimal.animal.height.min).from(QAnimal.animal))}
     */
    ComparableExpressionBase<?> max(ComparableExpressionBase<?> property);

}
