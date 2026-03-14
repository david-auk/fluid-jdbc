package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.factories;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class InsertQueryFactory {
    private InsertQueryFactory() {}

    /**
     * 1) Generates a complete SELECT query:
     * SELECT base.* FROM base [JOIN ...] [WHERE ...] [ORDER BY ...]
     */
    public static String build(Class<? extends TableEntity> tableEntity) {
        return InsertClause.build(tableEntity);
    }

    public static <TE extends TableEntity> void prepareInsertStatement(PreparedStatement insertStatement, TE entity) throws SQLException, IllegalAccessException {
        InsertClause.prepareInsertStatement(insertStatement, entity);
    }
}
