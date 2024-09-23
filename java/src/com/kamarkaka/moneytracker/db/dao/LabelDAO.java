package com.kamarkaka.moneytracker.db.dao;

import com.kamarkaka.jooq.model.tables.records.LabelRecord;
import com.kamarkaka.moneytracker.core.model.Label;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class LabelDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelDAO.class);

    private final com.kamarkaka.jooq.model.tables.Label labelTable = com.kamarkaka.jooq.model.tables.Label.LABEL;

    public LabelDAO() {}

    /**
     * CRUD operations
     */
    public int create(Label label) {
        return DSLContextFactory.getDSLContext().insertInto(labelTable, labelTable.NAME)
                .values(label.getName())
                .returningResult(labelTable.LABEL_ID)
                .fetchOne()
                .into(Integer.class);
    }

    public List<Label> read() {
        Stream<LabelRecord> resultStream = DSLContextFactory.getDSLContext().selectFrom(labelTable).fetchStream();
        return resultStream.map(r -> new Label(r.getLabelId(), r.getName())).toList();
    }

    public int update(Label label) {
        return DSLContextFactory.getDSLContext().update(labelTable)
                .set(labelTable.NAME, label.getName())
                .where(labelTable.LABEL_ID.eq(label.getId()))
                .execute();
    }

    public int delete(Label label) {
        return DSLContextFactory.getDSLContext().deleteFrom(labelTable)
                .where(labelTable.LABEL_ID.eq(label.getId()))
                .execute();
    }

    public Optional<Label> getById(int id) {
        LabelRecord result = DSLContextFactory.getDSLContext().selectFrom(labelTable).where(labelTable.LABEL_ID.eq(id)).fetchOne();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new Label(result.getLabelId(), result.getName()));
    }

    public Set<Label> getByIds(List<Integer> labelIds) {
        Result<LabelRecord> results = DSLContextFactory.getDSLContext().selectFrom(labelTable).where(labelTable.LABEL_ID.in(labelIds)).fetch();
        return new HashSet<>(results.map(r -> new Label(r.getLabelId(), r.getName())));
    }

    public Optional<Label> getByName(String name) {
        LabelRecord result = DSLContextFactory.getDSLContext().selectFrom(labelTable).where(labelTable.NAME.eq(name)).fetchOne();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new Label(result.getLabelId(), result.getName()));
    }}
