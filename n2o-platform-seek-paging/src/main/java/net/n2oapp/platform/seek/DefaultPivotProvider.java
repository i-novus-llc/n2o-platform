package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class DefaultPivotProvider implements PivotProvider {

    private static final Map<Class<?>, Object> MIN_TABLE = Map.of(
        String.class, "",
        LocalDate.class, LocalDate.of(-4711, 12, 31),
        LocalDateTime.class, LocalDate.of(-4712, 12, 31).atStartOfDay(),
        Short.class, Short.MIN_VALUE,
        Integer.class, Integer.MIN_VALUE,
        Long.class, Long.MIN_VALUE,
        UUID.class, UUID.fromString("00000000-0000-0000-0000-000000000000")
    );

    private static final Map<Class<?>, Object> MAX_TABLE = Map.of(
        String.class, Character.toString(0x10FFFF),
        LocalDate.class, LocalDate.of(5874896, 12, 31),
        LocalDateTime.class, LocalDate.of(294275, 12, 31).atStartOfDay(),
        Short.class, Short.MAX_VALUE,
        Integer.class, Integer.MAX_VALUE,
        Long.class, Long.MAX_VALUE,
        UUID.class, UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")
    );

    @Override
    @SuppressWarnings("unchecked")
    public <T> T min(ComparableExpressionBase<?> property) {
        return (T) MIN_TABLE.get(property.getType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T max(ComparableExpressionBase<?> property) {
        return (T) MAX_TABLE.get(property.getType());
    }
    
}
