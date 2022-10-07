package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.NotNull;

/**
 * A container for the <code>boolean</code> SQL type.
 */
public class BooleanContainer extends Container<Boolean> {
    /**
     * Create an instance of the container with raw content.
     * Raw value must be a valid boolean representation.
     * @param boxValue Raw content to unbox into this container
     */
    public BooleanContainer(String boxValue) { super(boxValue); }

    @Override
    protected Boolean unbox(String val) {
        // Manually parse the string to strictly only allow true or false values
        return val == null ? null : val.equals("true") ? Boolean.TRUE : val.equals("false") ? false : null;
    }

    @Override
    public String getFinalValue(@NotNull String input) {
        if (!isValid(input)) return null;
        if (!input.equals("true") && !input.equals("false")) return null;
        else return input;
    }

    @Override
    public boolean isValid(String value) {
        if (value == null) return false;
        // Average one liner fan
        return value.equals("true".substring(0, Math.min(value.length(), 4)))
            || value.equals("false".substring(0, Math.min(value.length(), 5)));
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }

    @Override
    public String toSQLString() {
        return toString() == null ? "null" : toString();
    }
}
