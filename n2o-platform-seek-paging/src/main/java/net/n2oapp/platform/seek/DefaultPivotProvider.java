package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultPivotProvider implements PivotProvider {

    private static final Map<Class<?>, Object> MIN_TABLE;
    static {
        MIN_TABLE = new HashMap<>();
        MIN_TABLE.put(String.class, "");
        MIN_TABLE.put(LocalDate.class, LocalDate.of(-4711, 12, 31));
        MIN_TABLE.put(LocalDateTime.class, LocalDate.of(-4712, 12, 31).atStartOfDay());
        MIN_TABLE.put(Short.class, Short.MIN_VALUE);
        MIN_TABLE.put(Integer.class, Integer.MIN_VALUE);
        MIN_TABLE.put(Long.class, Long.MIN_VALUE);
        MIN_TABLE.put(UUID.class, UUID.fromString("00000000-0000-0000-0000-000000000000"));
        MIN_TABLE.put(Double.class, Double.MIN_VALUE);
        MIN_TABLE.put(Float.class, Float.MIN_VALUE);
        MIN_TABLE.put(BigDecimal.class, BigDecimal.valueOf(Double.MIN_VALUE));
        MIN_TABLE.put(BigInteger.class, BigInteger.valueOf(Long.MIN_VALUE));
    }

    private static final Map<Class<?>, Object> MAX_TABLE;
    static {
        MAX_TABLE = new HashMap<>();
        MAX_TABLE.put(String.class, Character.toString(0x10FFFF));
        MAX_TABLE.put(LocalDate.class, LocalDate.of(5874896, 12, 31));
        MAX_TABLE.put(LocalDateTime.class, LocalDate.of(294275, 12, 31).atStartOfDay());
        MAX_TABLE.put(Short.class, Short.MAX_VALUE);
        MAX_TABLE.put(Integer.class, Integer.MAX_VALUE);
        MAX_TABLE.put(Long.class, Long.MAX_VALUE);
        MAX_TABLE.put(UUID.class, UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"));
        MAX_TABLE.put(Double.class, Double.MAX_VALUE);
        MAX_TABLE.put(Float.class, Float.MAX_VALUE);
        MAX_TABLE.put(BigDecimal.class, BigDecimal.valueOf(Double.MAX_VALUE));
        MAX_TABLE.put(BigInteger.class, BigInteger.valueOf(Long.MAX_VALUE));
    }

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
