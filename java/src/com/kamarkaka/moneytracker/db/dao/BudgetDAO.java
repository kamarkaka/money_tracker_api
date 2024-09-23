package com.kamarkaka.moneytracker.db.dao;

import com.kamarkaka.jooq.model.tables.records.BudgetRecord;
import com.kamarkaka.moneytracker.core.model.Budget;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BudgetDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetDAO.class);

    private final com.kamarkaka.jooq.model.tables.Budget budgetTable = com.kamarkaka.jooq.model.tables.Budget.BUDGET;

    public BudgetDAO() {}

    /**
     * CRUD operations
     */
    public int create(Budget budget) {
        Integer[] categoryIds = budget.getCategoryIds().toArray(new Integer[0]);

        return DSLContextFactory.getDSLContext().insertInto(budgetTable, budgetTable.NAME, budgetTable.CATEGORY_IDS, budgetTable.AMOUNT)
                .values(budget.getName(), categoryIds, budget.getAmount())
                .returningResult(budgetTable.BUDGET_ID)
                .fetchOne()
                .into(Integer.class);
    }

    public List<Budget> read() {
        Stream<BudgetRecord> resultStream = DSLContextFactory.getDSLContext().selectFrom(budgetTable).fetchStream();
        return resultStream.map(r -> new Budget(r.getBudgetId(), r.getName(), Arrays.stream(r.getCategoryIds()).toList(), r.getAmount())).toList();
    }

    public int update(Budget budget) {
        Integer[] categoryIds = budget.getCategoryIds().toArray(new Integer[0]);

        return DSLContextFactory.getDSLContext().update(budgetTable)
                .set(budgetTable.NAME, budget.getName())
                .set(budgetTable.CATEGORY_IDS, categoryIds)
                .set(budgetTable.AMOUNT, budget.getAmount())
                .where(budgetTable.BUDGET_ID.eq(budget.getId()))
                .execute();
    }

    public int delete(Budget budget) {
        return DSLContextFactory.getDSLContext().deleteFrom(budgetTable)
                .where(budgetTable.BUDGET_ID.eq(budget.getId()))
                .execute();
    }

    public Optional<Budget> getById(int budgetId) {
        BudgetRecord record = DSLContextFactory.getDSLContext().selectFrom(budgetTable)
                                .where(budgetTable.BUDGET_ID.eq(budgetId))
                                .fetchOne();
        if (record == null) {
            return Optional.empty();
        }

        return Optional.of(new Budget(
                record.getBudgetId(),
                record.getName(),
                Arrays.stream(record.getCategoryIds()).toList(),
                record.getAmount()
        ));
    }

}
