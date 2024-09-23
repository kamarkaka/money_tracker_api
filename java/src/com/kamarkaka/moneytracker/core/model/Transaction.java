package com.kamarkaka.moneytracker.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class Transaction {
    @NotNull
    private final long id;

    @NotNull
    private final LocalDate date;

    @NotNull
    private final String description;

    private final Account account;

    private final Category category;

    @NotNull
    private final Set<Label> labels;

    @NotNull
    private final boolean isHidden;

    @NotNull
    private final boolean isPending;

    @NotNull
    private final boolean isDuplicated;

    @NotNull
    private final BigDecimal amount;

    public Transaction(long id, LocalDate date, String description, Account account, Category category, Set<Label> labels, boolean isHidden, boolean isPending, boolean isDuplicated, BigDecimal amount) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.account = account;
        this.category = category;
        this.labels = labels;
        this.isHidden = isHidden;
        this.isPending = isPending;
        this.isDuplicated = isDuplicated;
        this.amount = amount;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public LocalDate getDate() {
        return date;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @JsonProperty
    public Account getAccount() {
        return account;
    }

    @JsonProperty
    public Category getCategory() {
        return category;
    }

    @JsonProperty
    public Set<Label> getLabels() {
        return labels;
    }

    @JsonProperty
    public boolean isHidden() {
        return isHidden;
    }

    @JsonProperty
    public boolean isPending() {
        return isPending;
    }

    @JsonProperty
    public boolean isDuplicated() {
        return isDuplicated;
    }

    @JsonProperty
    public BigDecimal getAmount() {
        return amount;
    }
}
