package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;

import java.util.Map;

public class AnimalPivotProvider extends DefaultPivotProvider {

    private static final Map<ComparableExpressionBase<?>, ComparableExpressionBase<?>> MIN = Map.of(
        QAnimal.animal.id, Expressions.asComparable(JPAExpressions.select(QAnimal.animal.id.min()).from(QAnimal.animal)),
        QAnimal.animal.height, Expressions.asComparable(JPAExpressions.select(QAnimal.animal.height.min()).from(QAnimal.animal))
    );

    private static final Map<ComparableExpressionBase<?>, ComparableExpressionBase<?>> MAX = Map.of(
        QAnimal.animal.id, Expressions.asComparable(JPAExpressions.select(QAnimal.animal.id.max()).from(QAnimal.animal)),
        QAnimal.animal.height, Expressions.asComparable(JPAExpressions.select(QAnimal.animal.height.max()).from(QAnimal.animal))
    );

    @Override
    public ComparableExpressionBase<?> min(ComparableExpressionBase<?> property) {
        return MIN.getOrDefault(property, super.min(property));
    }

    @Override
    public ComparableExpressionBase<?> max(ComparableExpressionBase<?> property) {
        return MAX.getOrDefault(property, super.max(property));
    }

}
