package com.cryptoalgo.oursql.model.db.data;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * A "box" for holding different types of data
 */
public abstract class Container<T> {
    public abstract T getValue();

    public Container(String boxValue) {}

    /**
     * @param value Value to be checked
     * @return true if <code>value</code> can be cast to the type of this container
     */
    public abstract boolean isValid(String value);

    public boolean isEditable() { return true; }

    // Fast lookup for the corresponding container classes for a certain SQL type
    static private final HashMap<String, Class<? extends Container<?>>> typeLookup = new HashMap<>();
    static {
        typeLookup.put("char", StringContainer.class);
        typeLookup.put("text", StringContainer.class);
        typeLookup.put("varchar", StringContainer.class);
        typeLookup.put("nchar", StringContainer.class);
        typeLookup.put("ntext", StringContainer.class);
        typeLookup.put("nvarchar", StringContainer.class);
        typeLookup.put("tid", TIDContainer.class);
    }

    /**
     *
     * @param forType SQL type to get container fo
     * @return Container class appropriate for type
     */
    @Nullable
    public static Class<? extends Container<?>> getInstance(String forType) {
        return typeLookup.getOrDefault(forType.toLowerCase(), null);
    }

    @Override
    public abstract String toString();
}
