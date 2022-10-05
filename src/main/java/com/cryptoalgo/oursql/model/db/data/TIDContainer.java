package com.cryptoalgo.oursql.model.db.data;

import java.util.Arrays;

/**
 * Container for the "tid" (tuple id) SQL type
 */
public class TIDContainer extends Container<int[]> {
    private final int[] ids;

    public TIDContainer(String boxValue) {
        super(boxValue);
        // It is assumed the db will never send malformed TIDs
        ids = Arrays.stream(boxValue.substring(1, boxValue.length()-1).split(","))
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    @Override
    public int[] getValue() {
        return ids;
    }

    @Override
    public boolean isValid(String value) {
        return false; // TIDs should never be updated
    }

    @Override
    public String toString() {
        return "("
            + Arrays
                .stream(ids)
                .mapToObj(String::valueOf)
                .reduce((a, b) -> a.concat(",").concat(b))
                .orElse("<null>")
            + ")";
    }
}
