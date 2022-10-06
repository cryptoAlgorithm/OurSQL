package com.cryptoalgo.oursql.model.db.data;

import java.util.Arrays;

/**
 * Container for the "tid" (tuple id) SQL type
 */
public class TIDContainer extends Container<int[]> {
    /**
     * Create an instance of a TID container with a raw string
     * @param boxValue Raw string to unbox into this container
     */
    public TIDContainer(String boxValue) { super(boxValue); }

    @Override
    protected int[] unbox(String val) {
        // It is assumed the db will never send malformed TIDs
        return Arrays.stream(val.substring(1, val.length()-1).split(","))
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    @Override
    public boolean isValid(String value) {
        return false; // TIDs should never be updated
    }

    @Override
    public String toString() {
        return "("
            + Arrays
                .stream(value)
                .mapToObj(String::valueOf)
                .reduce((a, b) -> a.concat(",").concat(b))
                .orElse("<null>")
            + ")";
    }
}
