package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * A container for int-based SQL types
 */
public class IntNumberContainer extends NumberContainer<Integer> {
    /**
     * Create an instance of this container with a string to decode and unbox.
     * Construction will fail if the provided raw string cannot be parsed as an int.
     * @param boxValue Raw string to decode and unbox into this container
     */
    public IntNumberContainer(String boxValue) { super(boxValue); }

    @Override
    public Integer unbox(String val) {
        if (val == null) return null;
        return Integer.parseInt(val);
    }

    @Override
    protected BigDecimal getMin() { return BigDecimal.valueOf(Integer.MIN_VALUE); }

    @Override
    protected BigDecimal getMax() { return BigDecimal.valueOf(Integer.MAX_VALUE); }

    @Override
    protected BigDecimal getBigDecimalValue(Integer val) { return BigDecimal.valueOf(val); }

    @Override
    public boolean isValid(@Nullable String value) {
        if (value == null) return false;
        // Ensure this is an integer (no dp) (loosely validated)
        return Pattern.matches("^-?\\d*$", value);
    }

    @Override
    public String toString() {
        return getValue() == null ? null : getValue().toString();
    }
}
