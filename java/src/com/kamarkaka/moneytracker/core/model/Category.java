package com.kamarkaka.moneytracker.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class Category {
    @NotNull
    private final int id;

    @NotNull
    private final String name;

    private final Integer parentId;

    @NotNull
    private final List<Category> children;

    public Category(int id, String name, Integer parentId, List<Category> children) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.children = children;
    }

    @JsonProperty
    public int getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Integer getParentId() {
        return parentId;
    }

    @JsonProperty
    public List<Category> getChildren() {
        return children;
    }
}
