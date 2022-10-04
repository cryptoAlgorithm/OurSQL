package com.cryptoalgo.oursql.model.db.data;

/**
 * Dummy container for unsupported values
 */
public class PlaceholderContainer extends Container<Void> {
    /**
     * Dummy constructor for maximum placeholder-ness
     */
    public PlaceholderContainer() { this(null); }
    public PlaceholderContainer(String boxValue) {
        super(boxValue);
    }

    @Override
    public boolean isEditable() { return false; }

    @Override
    Void getValue() {
        return null;
    }

    @Override
    boolean isValid(String value) {
        return false;
    }

    @Override
    public String toString() {
        return "";
    }
}
