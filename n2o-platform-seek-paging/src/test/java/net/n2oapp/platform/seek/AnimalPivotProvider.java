package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

import java.math.BigInteger;
import java.util.Map;

public class AnimalPivotProvider extends DefaultPivotProvider {

    private static final Map<ComparableExpressionBase<?>, Object> MIN = Map.of(
        QAnimal.animalEntity.height, BigInteger.valueOf(0)
    );

    private static final Map<ComparableExpressionBase<?>, Object> MAX = Map.of(
        QAnimal.animalEntity.height, BigInteger.valueOf(1000)
    );

    @Override
    @SuppressWarnings("unchecked")
    public <T> T min(ComparableExpressionBase<?> property) {
        return (T) MIN.getOrDefault(property, super.min(property));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T max(ComparableExpressionBase<?> property) {
        return (T) MAX.getOrDefault(property, super.max(property));
    }

}
