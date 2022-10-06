package com.cryptoalgo.oursql.model.db.data;

import java.math.BigDecimal;

public abstract class NumberContainer<T extends Number> extends Container<T> {
    public NumberContainer(String boxValue) { super(boxValue); }

    abstract BigDecimal getMin();
    abstract BigDecimal getMax();

    @Override
    public boolean isValid(String value) {

        return false;
    }

    @Override
    public abstract String toString();
}
