package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

public class TestParentEntityNullabilityProvider implements NullabilityProvider {
    @Override
    public boolean nullable(ComparableExpressionBase<?> property) {
        return property != QTestParentEntity.testParentEntity.id;
    }
}
