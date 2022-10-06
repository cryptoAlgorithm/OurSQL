package com.cryptoalgo.oursql.model.db.data;

import java.math.BigDecimal;

public class IntNumberContainer extends NumberContainer<Integer> {
    public IntNumberContainer(String boxValue) {
        super(boxValue);
    }

    @Override
    public Integer unbox(String val) {
        if (val == null) return null;
        return Integer.parseInt(val);
    }

    @Override
    BigDecimal getMin() {
        return BigDecimal.valueOf(Integer.MIN_VALUE);
    }

    @Override
    BigDecimal getMax() {
        return BigDecimal.valueOf(Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return getValue() == null ? null : getValue().toString();
    }
}
