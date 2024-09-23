package com.kamarkaka.moneytracker.db.dao;

import com.kamarkaka.jooq.model.tables.records.AccountRecord;
import com.kamarkaka.moneytracker.core.model.Account;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AccountDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDAO.class);

    private final com.kamarkaka.jooq.model.tables.Account accountTable = com.kamarkaka.jooq.model.tables.Account.ACCOUNT;

    public AccountDAO() {}

    /**
     * CRUD operations
     */
    public int create(Account account) {
        return DSLContextFactory.getDSLContext().insertInto(accountTable, accountTable.NAME)
                .values(account.getName())
                .returningResult(accountTable.ACCOUNT_ID)
                .fetchOne()
                .into(Integer.class);
    }

    public List<Account> read() {
        Stream<AccountRecord> resultStream = DSLContextFactory.getDSLContext().selectFrom(accountTable).fetchStream();
        return resultStream.map(r -> new Account(r.getAccountId(), r.getName())).toList();
    }

    public int update(Account account) {
        return DSLContextFactory.getDSLContext().update(accountTable)
                .set(accountTable.NAME, account.getName())
                .where(accountTable.ACCOUNT_ID.eq(account.getId()))
                .execute();
    }

    public int delete(Account account) {
        return DSLContextFactory.getDSLContext().deleteFrom(accountTable)
                .where(accountTable.ACCOUNT_ID.eq(account.getId()))
                .execute();
    }

    public Optional<Account> getById(int id) {
        AccountRecord result = DSLContextFactory.getDSLContext().selectFrom(accountTable).where(accountTable.ACCOUNT_ID.eq(id)).fetchOne();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new Account(result.getAccountId(), result.getName()));
    }

    public Optional<Account> getByName(String name) {
        AccountRecord result = DSLContextFactory.getDSLContext().selectFrom(accountTable).where(accountTable.NAME.eq(name)).fetchOne();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new Account(result.getAccountId(), result.getName()));
    }
}
