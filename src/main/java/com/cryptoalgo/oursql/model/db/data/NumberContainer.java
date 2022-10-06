package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * An abstract container to store various numerical SQL types
 * @param <T> Numerical type for the container.
 */
public abstract class NumberContainer<T extends Number> extends Container<T> {
    /**
     * Create an instance of a <code>NumberContainer</code> with a raw value.
     * @param boxValue Raw value to unbox into container
     */
    public NumberContainer(String boxValue) { super(boxValue); }

    /**
     * Get the minimum allowable value that can be represented by this numerical type
     * @return Minimum value that can be represented by this numerical type
     */
    protected abstract BigDecimal getMin();

    /**
     * Get the maximum allowable value that can be represented by this numerical type
     * @return Maximum value that can be represented by this numerical type
     */
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
        return unbox(input).toString(); // To ensure the number is free of leading/trailing zeros
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
