package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.factories;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause.InsertClause;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause.UpdateClause;

import java.sql.PreparedStatement;

public final class UpdateQueryFactory {
    private UpdateQueryFactory() {}

    public static String build(Class<? extends TableEntity> tableEntity, boolean updatePrimaryKey) {
        return UpdateClause.build(tableEntity, updatePrimaryKey);
    }

    public static <TE extends TableEntity> void prepareUpdateStatement(PreparedStatement updateStatement, TE entity) {
        UpdateClause.prepareUpdateStatement(updateStatement, entity);
    }

    public static <TE extends TableEntity> void prepareUpdateStatement(PreparedStatement updateStatement, TE originalEntity, TE newEntity) {
        UpdateClause.prepareUpdateStatement(updateStatement, originalEntity, newEntity, true);
    }
}
