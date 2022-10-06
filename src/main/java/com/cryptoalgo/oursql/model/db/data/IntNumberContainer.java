package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class IntNumberContainer extends NumberContainer<Integer> {
    public IntNumberContainer(String boxValue) {
        super(boxValue);
    }

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
