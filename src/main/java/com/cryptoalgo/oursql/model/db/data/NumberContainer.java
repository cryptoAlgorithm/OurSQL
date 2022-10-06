package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public abstract class NumberContainer<T extends Number> extends Container<T> {
    public NumberContainer(String boxValue) { super(boxValue); }

    protected abstract BigDecimal getMin();
    protected abstract BigDecimal getMax();

    /**
     * Get the {@link BigDecimal} representation of a value of the container's boxed type
     * @param val A value
     * @return The {@link BigDecimal} representation of the value
     */
    protected abstract BigDecimal getBigDecimalValue(T val);

    @Override
    public String getFinalValue(@NotNull String input) {
        if (!isValid(input)) return null;
        if (input.equals("-")) return "0";
        if (input.endsWith(".")) return input.substring(0, input.length()-1);
        if (!Pattern.matches("^-?\\d+\\.?", input)) {
            if (input.startsWith("-")) return "-0" + input.substring(input.indexOf("."));
            else return 0 + input; // one of the string concatenations of all time
        }
        try {
            final var bigDecVal = getBigDecimalValue(unbox(input));
            if (bigDecVal.compareTo(getMin()) < 0) return getMin().toString();
            if (bigDecVal.compareTo(getMax()) > 0) return getMax().toString();
        } catch (NumberFormatException e) { return null; }
        return input;
    }

    @Override
    public boolean isValid(@Nullable String value) {
        if (value == null) return false;
        // Ensure this is a number (loosely validated)
        return Pattern.matches("^-?\\d*\\.?\\d*$", value);
    }

    @Override
    public abstract String toString();
}
