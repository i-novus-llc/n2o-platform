package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

/**
 * Пустая реализация {@link NullabilityProvider}.
 * Всегда стоит переопределить ее для улучшения производительности
 */
public class DefaultNullabilityProvider implements NullabilityProvider {
    @Override
    public boolean nullable(ComparableExpressionBase<?> property) {
        return true;
    }
}
