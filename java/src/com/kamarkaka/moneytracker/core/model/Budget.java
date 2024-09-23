package com.kamarkaka.moneytracker.core.model;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class Budget {
    @NotNull
    private final int id;

    @NotNull
    private final String name;

    @NotNull
    private final List<Integer> categoryIds;

    @NotNull
    private final BigDecimal amount;

    public Budget(int id, String name, List<Integer> categoryIds, BigDecimal amount) {
        this.id = id;
        this.name = name;
        this.categoryIds = categoryIds;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getCategoryIds() {
        return categoryIds;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
