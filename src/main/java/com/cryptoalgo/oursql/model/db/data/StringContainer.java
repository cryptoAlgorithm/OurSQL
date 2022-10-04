package com.cryptoalgo.oursql.model.db.data;

public class StringContainer extends Container<String> {
    private final String boxed;

    public StringContainer(String boxValue) {
        super(boxValue);
        boxed = boxValue;
    }

    @Override
    String getValue() { return boxed; }

    @Override
    boolean isValid(String value) {
        return true; // All strings can be boxed in this container
    }

    @Override
    public String toString() {
        return boxed;
    }
}
