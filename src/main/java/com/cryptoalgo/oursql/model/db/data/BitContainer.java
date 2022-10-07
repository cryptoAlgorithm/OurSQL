package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.NotNull;

/**
 * A container for the <code>bit</code> SQL type.
 */
public class BitContainer extends Container<Boolean> {
    /**
     * Create an instance of the container with raw content.
     * Raw value must be a valid boolean representation.
     * @param boxValue Raw content to unbox into this container
     */
    public BitContainer(String boxValue) { super(boxValue); }

    @Override
    protected Boolean unbox(String val) {
        // Manually parse the string to strictly only allow true or false values
        // for some reason the database randomly decided to return "t" and "f" instead of "true" and "false"
        return val == null
            ? null
            : val.equals("1")
            ? Boolean.TRUE
            : (val.equals("0"))
            ? false
            : null;
    }

    @Override
    public String getFinalValue(@NotNull String input) {
        if (!isValid(input)) return null;
        return input;
    }

    @Override
    public boolean isValid(String value) {
        if (value == null) return false;
        return value.isEmpty() || value.equals("1") || value.equals("0");
    }

    @Override
    public String toString() { return value == null ? null : value ? "1" : "0"; }

    @Override
    public String toSQLString() {
        return toString() == null ? "null" : "'" + this + "'";
    }
}
