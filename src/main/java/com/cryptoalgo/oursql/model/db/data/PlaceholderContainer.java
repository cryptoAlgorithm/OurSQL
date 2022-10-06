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
    protected Void unbox(String val) { return null; }

    @Override
    public boolean isEditable() { return false; }

    @Override
    public boolean isValid(String value) {
        return false;
    }

    @Override
    public String toString() {
        return "";
    }
}
