package com.cryptoalgo.oursql.model.db.data;

public class StringContainer extends Container<String> {
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
