package net.n2oapp.platform.seek;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultPivotProvider implements PivotProvider {

    private static final Map<Class<?>, ComparableExpressionBase<?>> MIN_TABLE;
    static {
        MIN_TABLE = new HashMap<>();
        MIN_TABLE.put(String.class, Expressions.asComparable(""));
        MIN_TABLE.put(LocalDate.class, Expressions.asComparable(LocalDate.of(-4711, 12, 31)));
        MIN_TABLE.put(LocalDateTime.class, Expressions.asComparable(LocalDate.of(-4712, 12, 31).atStartOfDay()));
        MIN_TABLE.put(Short.class, Expressions.asComparable(Short.MIN_VALUE));
        MIN_TABLE.put(Integer.class, Expressions.asComparable(Integer.MIN_VALUE));
        MIN_TABLE.put(Long.class, Expressions.asComparable(Long.MIN_VALUE));
        MIN_TABLE.put(UUID.class, Expressions.asComparable(UUID.fromString("00000000-0000-0000-0000-000000000000")));
        MIN_TABLE.put(Double.class, Expressions.asComparable(Double.MIN_VALUE));
        MIN_TABLE.put(Float.class, Expressions.asComparable(Float.MIN_VALUE));
        MIN_TABLE.put(BigDecimal.class, Expressions.asComparable(BigDecimal.valueOf(Double.MIN_VALUE)));
        MIN_TABLE.put(BigInteger.class, Expressions.asComparable(BigInteger.valueOf(Long.MIN_VALUE)));
    }

    private static final Map<Class<?>, ComparableExpressionBase<?>> MAX_TABLE;
    static {
        MAX_TABLE = new HashMap<>();
        MAX_TABLE.put(String.class, Expressions.asComparable(Character.toString(0x10FFFF)));
        MAX_TABLE.put(LocalDate.class, Expressions.asComparable(LocalDate.of(5874896, 12, 31)));
        MAX_TABLE.put(LocalDateTime.class, Expressions.asComparable(LocalDate.of(294275, 12, 31).atStartOfDay()));
        MAX_TABLE.put(Short.class, Expressions.asComparable(Short.MAX_VALUE));
        MAX_TABLE.put(Integer.class, Expressions.asComparable(Integer.MAX_VALUE));
        MAX_TABLE.put(Long.class, Expressions.asComparable(Long.MAX_VALUE));
        MAX_TABLE.put(UUID.class, Expressions.asComparable(UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")));
        MAX_TABLE.put(Double.class, Expressions.asComparable(Double.MAX_VALUE));
        MAX_TABLE.put(Float.class, Expressions.asComparable(Float.MAX_VALUE));
        MAX_TABLE.put(BigDecimal.class, Expressions.asComparable(BigDecimal.valueOf(Double.MAX_VALUE)));
        MAX_TABLE.put(BigInteger.class, Expressions.asComparable(BigInteger.valueOf(Long.MAX_VALUE)));
    }

    @Override
    public ComparableExpressionBase<?> min(ComparableExpressionBase<?> property) {
        return MIN_TABLE.get(property.getType());
    }

    @Override
    public ComparableExpressionBase<?> max(ComparableExpressionBase<?> property) {
        return MAX_TABLE.get(property.getType());
    }

}
