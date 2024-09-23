package com.kamarkaka.moneytracker.db.dao;

import com.kamarkaka.jooq.model.tables.records.AccountRecord;
import com.kamarkaka.jooq.model.tables.records.CategoryRecord;
import com.kamarkaka.jooq.model.tables.records.TransactionRecord;
import com.kamarkaka.moneytracker.core.model.*;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import com.kamarkaka.moneytracker.db.TransactionFilter;
import com.kamarkaka.moneytracker.db.TransactionFilterBuilder;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.jooq.impl.DSL.*;

public class TransactionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionDAO.class);

    private final com.kamarkaka.jooq.model.tables.Account accountTable = com.kamarkaka.jooq.model.tables.Account.ACCOUNT;
    private final com.kamarkaka.jooq.model.tables.Category categoryTable = com.kamarkaka.jooq.model.tables.Category.CATEGORY;
    private final com.kamarkaka.jooq.model.tables.Transaction transactionTable = com.kamarkaka.jooq.model.tables.Transaction.TRANSACTION;

    private final LabelDAO labelDAO;

    public TransactionDAO(LabelDAO labelDAO) {
        this.labelDAO = labelDAO;
    }

    /**
     * CRUD operations
     */
    public int create(Transaction transaction) {
        Integer accountId = transaction.getAccount().getId();
        Integer categoryId = transaction.getCategory() == null ? null : transaction.getCategory().getId();
        Integer[] labelIds = transaction.getLabels().stream().map(Label::getId).toArray(Integer[]::new);

        return DSLContextFactory.getDSLContext().insertInto(transactionTable).columns(
                        transactionTable.DATE,
                        transactionTable.DESCRIPTION,
                        transactionTable.ACCOUNT_ID,
                        transactionTable.CATEGORY_ID,
                        transactionTable.LABEL_IDS,
                        transactionTable.IS_HIDDEN,
                        transactionTable.IS_PENDING,
                        transactionTable.IS_DUPLICATED,
                        transactionTable.AMOUNT)
                .select(select(
                        val(transaction.getDate()),
                        val(transaction.getDescription()),
                        val(accountId),
                        val(categoryId),
                        val(labelIds),
                        val(transaction.isHidden()),
                        val(transaction.isPending()),
                        val(transaction.isDuplicated()),
                        val(transaction.getAmount()))
                        .where(notExists(selectOne().from(transactionTable).where(
                                transactionTable.DATE.eq(transaction.getDate()),
                                transactionTable.DESCRIPTION.eq(transaction.getDescription()),
                                transactionTable.ACCOUNT_ID.eq(accountId),
                                transactionTable.AMOUNT.eq(transaction.getAmount())
                        )))
                )
                .execute();
    }

    public int update(Transaction transaction) {
        var query = DSLContextFactory.getDSLContext().update(transactionTable)
                .set(transactionTable.DATE, transaction.getDate())
                .set(transactionTable.DESCRIPTION, transaction.getDescription())
                .set(transactionTable.ACCOUNT_ID, transaction.getAccount() == null ? null : transaction.getAccount().getId())
                .set(transactionTable.CATEGORY_ID, transaction.getCategory() == null ? null : transaction.getCategory().getId())
                .set(transactionTable.LABEL_IDS, transaction.getLabels().stream().map(Label::getId).toArray(Integer[]::new))
                .set(transactionTable.IS_HIDDEN, transaction.isHidden())
                .set(transactionTable.IS_PENDING, transaction.isPending())
                .set(transactionTable.IS_DUPLICATED, transaction.isDuplicated())
                .where(transactionTable.TRANSACTION_ID.eq(transaction.getId()));

        return query.execute();
    }

    public int delete(Transaction transaction) {
        return DSLContextFactory.getDSLContext().deleteFrom(transactionTable)
                .where(transactionTable.TRANSACTION_ID.eq(transaction.getId()))
                .execute();
    }

    public List<Transaction> listByFilter(TransactionFilter filter) {
        Result<Record3<TransactionRecord, AccountRecord, CategoryRecord>> queryResults =
                DSLContextFactory.getDSLContext().select(transactionTable, accountTable, categoryTable)
                  .from(transactionTable)
                  .leftJoin(accountTable).on(accountTable.ACCOUNT_ID.eq(transactionTable.ACCOUNT_ID))
                  .leftJoin(categoryTable).on(categoryTable.CATEGORY_ID.eq(transactionTable.CATEGORY_ID))
                  .where(filter.buildCondition())
                  .fetch();

        return queryResults.stream().map(this::createTransactionInstance).toList();
    }

    public List<Transaction> listUncategorized() {
        TransactionFilterBuilder builder = new TransactionFilterBuilder();
        TransactionFilter filter = builder.setShowTransactionWithNoCategory()
                                          .build();

        return listByFilter(filter);
    }

    public Optional<Transaction> getById(long id) {
        TransactionFilterBuilder builder = new TransactionFilterBuilder();
        TransactionFilter filter = builder.setTransactionId(id)
                .build();
        List<Transaction> result = listByFilter(filter);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    private Transaction createTransactionInstance(Record3<TransactionRecord, AccountRecord, CategoryRecord> record) {
        Account account = null;
        AccountRecord accountRecord = record.component2();
        if (accountRecord != null) {
            account = new Account(accountRecord.getAccountId(), accountRecord.getName());
        }

        Category category = null;
        CategoryRecord categoryRecord = record.component3();
        if (categoryRecord != null && categoryRecord.getCategoryId() != null) {
            category = new Category(categoryRecord.getCategoryId(), categoryRecord.getName(), categoryRecord.getParentId(), new ArrayList<>());
        }

        Set<Label> labels = new HashSet<>();
        Integer[] labelIds = record.component1().getLabelIds();
        if (labelIds != null && labelIds.length > 0) {
            labels = labelDAO.getByIds(Arrays.stream(labelIds).toList());
        }

        return new Transaction(
                record.component1().getTransactionId(),
                record.component1().getDate(),
                record.component1().getDescription(),
                account,
                category,
                labels,
                record.component1().getIsHidden(),
                record.component1().getIsPending(),
                record.component1().getIsDuplicated(),
                record.component1().getAmount()
        );
    }
}
