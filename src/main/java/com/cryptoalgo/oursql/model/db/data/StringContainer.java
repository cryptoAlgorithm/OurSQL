package com.cryptoalgo.oursql.model.db.data;

/**
 * A container for various text SQL types
 */
public class StringContainer extends Container<String> {
    /**
     * Create an instance of the container with raw content.
     * @param boxValue Raw content to unbox into this container
     */
    public StringContainer(String boxValue) { super(boxValue); }

    @Override
    protected String unbox(String val) { return val; }

    @Override
    public boolean isValid(String value) {
        return true; // All strings can be boxed in this container
    }

    @Override
    public String toString() {
        return value;
    }
}
