package com.cryptoalgo.oursql.model.db.data;

/**
 * A container for the <code>boolean</code> SQL type.
 */
public class BitContainer extends Container<Boolean> {
    /**
     * Create an instance of the container with raw content.
     * Raw value must be a valid boolean representation.
     * @param boxValue Raw content to unbox into this container
     */
    public BitContainer(String boxValue) { super(boxValue); }

    @Override
    protected Boolean unbox(String val) { return val == null ? null : Boolean.valueOf(val); }

    @Override
    public boolean isValid(String value) {
        return true; // All strings can be boxed in this container
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
