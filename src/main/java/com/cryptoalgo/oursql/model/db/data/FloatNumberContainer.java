package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * A container for the float SQL data type.
 * <p><b>INCOMPLETE!</b></p>
 */
public class FloatNumberContainer extends NumberContainer<Float> {
    /**
     * Create an instance of this container with a string to decode and unbox.
     * Construction will fail if the provided raw string cannot be parsed as a float.
     * @param boxValue Raw string to decode and unbox into this container
     */
    public FloatNumberContainer(String boxValue) { super(boxValue); }

    @Override
    public Float unbox(String val) {
        if (val == null) return null;
        return Float.parseFloat(val);
    }

    @Override
    protected BigDecimal getMin() { return BigDecimal.valueOf(Integer.MIN_VALUE); }

    @Override
    protected BigDecimal getMax() { return BigDecimal.valueOf(Integer.MAX_VALUE); }

    @Override
    protected BigDecimal getBigDecimalValue(Float val) { return BigDecimal.valueOf(val); }

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
