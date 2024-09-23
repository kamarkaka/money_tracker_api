package com.kamarkaka.moneytracker.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class Rule {
    @NotNull
    private final int id;

    @NotNull
    private final String description;

    @NotNull
    private final int categoryId;

    public Rule(int id, String description, int categoryId) {
        this.id = id;
        this.description = description;
        this.categoryId = categoryId;
    }

    @JsonProperty
    public int getId() {
        return id;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @JsonProperty
    public int getCategoryId() {
        return categoryId;
    }
}
