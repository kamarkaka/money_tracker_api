package com.kamarkaka.moneytracker.db;

public enum FilterOp {
    GREATER_THAN (">"),
    LESS_THAN ("<"),
    EQUAL_TO ("="),
    CONTAINS ("in");

    private final String value;

    FilterOp(String value) {
        this.value = value;
    }

    public static FilterOp of(String value) {
        for (FilterOp op : values()) {
            if (value.equalsIgnoreCase(op.value)) {
                return op;
            }
        }
        return null;
    }
}
