package com.kamarkaka.moneytracker.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionFilterBuilder {
    private Optional<List<Integer>> accountIds;
    private Optional<Float> amountMin;
    private Optional<Float> amountMax;
    private Optional<Integer> budgetId;
    private Optional<List<Integer>> categoryIds;
    private DateRange dateRange;
    private Optional<String> descriptionQueryStr;
    private boolean showTransactionWithNoCategory;
    private boolean showDuplicated;
    private boolean showHidden;
    private Optional<Long> transactionId;

    public TransactionFilterBuilder() {
        accountIds = Optional.empty();
        amountMin = Optional.empty();
        amountMax= Optional.empty();
        budgetId = Optional.empty();
        categoryIds = Optional.empty();
        dateRange = new DateRange(LocalDate.of(1970, 1, 1), LocalDate.now());
        descriptionQueryStr = Optional.empty();
        showTransactionWithNoCategory = false;
        showDuplicated = false;
        showHidden = false;
        transactionId = Optional.empty();
    }

    public TransactionFilterBuilder setAccountIds(List<Integer> accountIds) {
        if (accountIds != null && !accountIds.isEmpty()) {
            this.accountIds = Optional.of(accountIds);
        }
        return this;
    }

    public TransactionFilterBuilder setAmountMin(float amountMin) {
        if (amountMin > 0) {
            this.amountMin = Optional.of(amountMin);
        }
        return this;
    }

    public TransactionFilterBuilder setAmountMax(float amountMax) {
        if (amountMax > 0) {
            this.amountMax = Optional.of(amountMax);
        }
        return this;
    }

    public TransactionFilterBuilder setBudgetId(int budgetId, List<Integer> categoryIds) {
        if (budgetId != 0 && categoryIds != null && !categoryIds.isEmpty()) {
            this.budgetId = Optional.of(budgetId);
            this.categoryIds = Optional.of(categoryIds);
        }
        return this;
    }

    public TransactionFilterBuilder setCategoryIds(List<Integer> categoryIds) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            this.categoryIds = Optional.of(categoryIds);
        }
        return this;
    }

    public TransactionFilterBuilder setDateRange(LocalDate beginDate, LocalDate endDate) {
        if (beginDate != null && endDate != null && (beginDate.isBefore(endDate) || beginDate.isEqual(endDate))) {
            this.dateRange = new DateRange(beginDate, endDate);
        }
        return this;
    }

    public TransactionFilterBuilder setDescriptionQuery(String queryStr) {
        if (queryStr != null && !queryStr.isBlank()) {
            this.descriptionQueryStr = Optional.of(queryStr);
        }
        return this;
    }

    public TransactionFilterBuilder setShowTransactionWithNoCategory() {
        this.showTransactionWithNoCategory = true;
        return this;
    }

    public TransactionFilterBuilder setShowDuplicated() {
        this.showDuplicated = true;
        return this;
    }

    public TransactionFilterBuilder setShowHidden() {
        this.showHidden = true;
        return this;
    }

    public TransactionFilterBuilder setTransactionId(long transactionId) {
        if (transactionId > 0) {
            this.transactionId = Optional.of(transactionId);
        }
        return this;
    }

    public TransactionFilter build() {
        return new TransactionFilter(
            accountIds,
            amountMin,
            amountMax,
            budgetId,
            categoryIds,
            dateRange,
            descriptionQueryStr,
            showTransactionWithNoCategory,
            showDuplicated,
            showHidden,
            transactionId
        );
    }
}
