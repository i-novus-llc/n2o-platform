package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;

import java.util.Map;

public class AnimalPivotProvider extends DefaultPivotProvider {

    private static final Map<ComparableExpressionBase<?>, ComparableExpressionBase<?>> MIN = Map.of(
        QAnimal.animalEntity.id, Expressions.asComparable(JPAExpressions.select(QAnimal.animalEntity.id.min()).from(QAnimal.animalEntity)),
        QAnimal.animalEntity.height, Expressions.asComparable(JPAExpressions.select(QAnimal.animalEntity.height.min()).from(QAnimal.animalEntity))
    );

    private static final Map<ComparableExpressionBase<?>, ComparableExpressionBase<?>> MAX = Map.of(
        QAnimal.animalEntity.id, Expressions.asComparable(JPAExpressions.select(QAnimal.animalEntity.id.max()).from(QAnimal.animalEntity)),
        QAnimal.animalEntity.height, Expressions.asComparable(JPAExpressions.select(QAnimal.animalEntity.height.max()).from(QAnimal.animalEntity))
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
