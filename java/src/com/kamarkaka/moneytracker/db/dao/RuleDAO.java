package com.kamarkaka.moneytracker.db.dao;

import com.kamarkaka.jooq.model.tables.records.RuleRecord;
import com.kamarkaka.moneytracker.core.model.Rule;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class RuleDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleDAO.class);

    private final com.kamarkaka.jooq.model.tables.Rule ruleTable = com.kamarkaka.jooq.model.tables.Rule.RULE;

    public RuleDAO() {}

    /**
     * CRUD operations
     */
    public int create(Rule rule) {
        return DSLContextFactory.getDSLContext().insertInto(ruleTable, ruleTable.DESCRIPTION, ruleTable.RULE_ID)
                .values(rule.getDescription(), rule.getCategoryId())
                .returningResult(ruleTable.RULE_ID)
                .fetchOne()
                .into(Integer.class);
    }

    public List<Rule> read() {
        Stream<RuleRecord> resultStream = DSLContextFactory.getDSLContext().selectFrom(ruleTable).fetchStream();
        return resultStream.map(r -> new Rule(r.getRuleId(), r.getDescription(), r.getCategoryId())).toList();
    }

    public int update(Rule rule) {
        return DSLContextFactory.getDSLContext().update(ruleTable)
                .set(ruleTable.DESCRIPTION, rule.getDescription())
                .set(ruleTable.CATEGORY_ID, rule.getCategoryId())
                .where(ruleTable.RULE_ID.eq(rule.getId()))
                .execute();
    }

    public int delete(Rule rule) {
        return DSLContextFactory.getDSLContext().deleteFrom(ruleTable)
                .where(ruleTable.RULE_ID.eq(rule.getId()))
                .execute();
    }
}
