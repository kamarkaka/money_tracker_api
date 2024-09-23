package com.kamarkaka.moneytracker.db.dao;

import com.kamarkaka.jooq.model.tables.records.CategoryRecord;
import com.kamarkaka.moneytracker.core.model.Category;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CategoryDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDAO.class);

    private final com.kamarkaka.jooq.model.tables.Category categoryTable = com.kamarkaka.jooq.model.tables.Category.CATEGORY;

    public CategoryDAO() {}

    /**
     * CRUD operations
     */
    public int create(Category category) {
        return DSLContextFactory.getDSLContext().insertInto(categoryTable, categoryTable.NAME, categoryTable.PARENT_ID)
                .values(category.getName(), category.getParentId())
                .returningResult(categoryTable.CATEGORY_ID)
                .fetchOne()
                .into(Integer.class);
    }

    public List<Category> read() {
        Stream<CategoryRecord> resultStream = DSLContextFactory.getDSLContext().selectFrom(categoryTable).fetchStream();
        return resultStream.map(r -> new Category(r.getCategoryId(), r.getName(), r.getParentId(), new ArrayList<>())).toList();
    }

    public int update(Category category) {
        return DSLContextFactory.getDSLContext().update(categoryTable)
                .set(categoryTable.NAME, category.getName())
                .set(categoryTable.PARENT_ID, category.getParentId())
                .where(categoryTable.CATEGORY_ID.eq(category.getId()))
                .execute();
    }

    public int delete(Category category) {
        return DSLContextFactory.getDSLContext().deleteFrom(categoryTable)
                .where(categoryTable.CATEGORY_ID.eq(category.getId()))
                .execute();
    }

    public Optional<Category> getById(int id) {
        CategoryRecord result = DSLContextFactory.getDSLContext().selectFrom(categoryTable).where(categoryTable.CATEGORY_ID.eq(id)).fetchOne();
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(new Category(result.getCategoryId(), result.getName(), result.getParentId(), new ArrayList<>()));
    }
}
