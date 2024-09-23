package com.kamarkaka.moneytracker.db;

import com.kamarkaka.jooq.model.tables.Category;
import com.kamarkaka.jooq.model.tables.Transaction;
import org.jooq.Condition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.abs;

public class TransactionFilter {
    private static final Transaction transactionTable = Transaction.TRANSACTION;
    private static final Category categoryTable = Category.CATEGORY;

    private final Optional<List<Integer>> accountIds;
    private final Optional<Float> amountMin;
    private final Optional<Float> amountMax;
    private final Optional<Integer> budgetId;
    private final Optional<List<Integer>> categoryIds;
    private final DateRange dateRange;
    private final Optional<String> descriptionQueryStr;
    private final boolean showTransactionWithNoCategory;
    private final boolean showDuplicated;
    private final boolean showHidden;
    private final Optional<Long> transactionId;

    public TransactionFilter(
        Optional<List<Integer>> accountIds,
        Optional<Float> amountMin,
        Optional<Float> amountMax,
        Optional<Integer> budgetId,
        Optional<List<Integer>> categoryIds,
        DateRange dateRange,
        Optional<String> descriptionQueryStr,
        boolean showTransactionWithNoCategory,
        boolean showDuplicated,
        boolean showHidden,
        Optional<Long> transactionId
    ) {
        this.accountIds = accountIds;
        this.amountMin = amountMin;
        this.amountMax = amountMax;
        this.budgetId = budgetId;
        this.categoryIds = categoryIds;
        this.dateRange = dateRange;
        this.descriptionQueryStr = descriptionQueryStr;
        this.showTransactionWithNoCategory = showTransactionWithNoCategory;
        this.showDuplicated = showDuplicated;
        this.showHidden = showHidden;
        this.transactionId = transactionId;
    }

    public Condition buildCondition() {
        Condition condition = transactionTable.DATE.between(dateRange.getBeginDate(), dateRange.getEndDate());

        if (transactionId.isPresent()) {
            condition = transactionTable.TRANSACTION_ID.eq(transactionId.get());
            return condition;
        }

        if (accountIds.isPresent()) {
            condition = condition.and(transactionTable.ACCOUNT_ID.in(accountIds.get()));
        }

        if (amountMin.isPresent() && amountMax.isPresent()) {
            condition = condition.and(abs(transactionTable.AMOUNT).between(BigDecimal.valueOf(amountMin.get()),
                                                                           BigDecimal.valueOf(amountMax.get())));
        } else if (amountMin.isPresent()) {
            condition = condition.and(abs(transactionTable.AMOUNT).ge(BigDecimal.valueOf(amountMin.get())));
        } else if (amountMax.isPresent()) {
            condition = condition.and(abs(transactionTable.AMOUNT).le(BigDecimal.valueOf(amountMax.get())));
        }

        if (budgetId.isPresent() && categoryIds.isPresent()) {
            condition = condition.and(transactionTable.CATEGORY_ID.in(categoryIds.get()));
        } else if (categoryIds.isPresent()) {
            condition = condition.and(transactionTable.CATEGORY_ID.in(categoryIds.get()).or(categoryTable.PARENT_ID.in(categoryIds.get())));
        } else if (showTransactionWithNoCategory) {
            condition = condition.and(transactionTable.CATEGORY_ID.isNull());
        } else {
            condition = condition.and(transactionTable.CATEGORY_ID.isNotNull());
        }

        if (descriptionQueryStr.isPresent()) {
            condition = condition.and(transactionTable.DESCRIPTION.contains(descriptionQueryStr.get()));
        }

        if (!showDuplicated) {
            condition = condition.and(transactionTable.IS_DUPLICATED.isFalse());
        }

        if (!showHidden) {
            condition = condition.and(transactionTable.IS_HIDDEN.isFalse());
        }

        return condition;
    }
}
