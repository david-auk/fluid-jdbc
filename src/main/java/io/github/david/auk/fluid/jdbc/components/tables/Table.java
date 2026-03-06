package io.github.david.auk.fluid.jdbc.components.tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableEntityResolver;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Table<TE extends TableEntity, PK> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Class<TE> tableEntityClass;
    private final AccessibleObject primaryKeyMember;
    private final Class<PK> primaryKeyDataType;
    private final Map<Field, String> fieldToColumnName;
    private final TableEntityResolver<TE, PK> tableEntityResolver;

    protected final String tableName;

    @SuppressWarnings("unchecked")
    public Table(Class<TE> tableEntityClass) {

        this.tableEntityClass = tableEntityClass;

        this.tableName = TableUtils.getTableName(this.tableEntityClass);

        this.fieldToColumnName = TableUtils.mapFieldToColumnNames(this.tableEntityClass)
                .entrySet()
                .stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey().reflect(), entry.getValue()), HashMap::putAll);

        // pick up the @PrimaryKey (field or zero-arg method)
        this.primaryKeyMember = TableUtils.getPrimaryKeyMember(this.tableEntityClass);
        this.primaryKeyDataType = (Class<PK>) TableUtils.getPrimaryKeyType(this.tableEntityClass);

        this.tableEntityResolver = new TableEntityResolver<>(
                this.tableEntityClass,
                this.primaryKeyDataType,
                this.fieldToColumnName,
                objectMapper
        );
    }

    public TE buildFromTableWildcardQuery(Connection connection, ResultSet rs) throws SQLException {
        return tableEntityResolver.buildFromTableWildcardQuery(connection, rs);
    }

    /**
     * Returns the raw PK value
     */
    @SuppressWarnings("unchecked")
    public PK getPrimaryKey(TE entity) {
        return (PK)  TableUtils.getPrimaryKeyValue(entity.getClass());
    }

    /**
     * If you need the actual column columnName for a single‐field PK.
     * Throws if you’re using a composite key.
     */
    public String getPrimaryKeyColumnName() {
        if (primaryKeyMember instanceof Field pkField) {
            // Prefer the resolved mapping (includes inherited fields if TableUtils is configured that way)
            String mapped = fieldToColumnName.get(pkField);
            if (mapped != null) {
                return mapped;
            }

            // Fallback: derive from @TableColumn on the PK field itself
            TableColumn tc = pkField.getAnnotation(TableColumn.class);
            if (tc != null && tc.columnName() != null && !tc.columnName().isEmpty()) {
                return tc.columnName();
            }
            return pkField.getName();
        }
        throw new UnsupportedOperationException(
                "Composite primary key — use getPrimaryKeyColumnNames()");
    }


    public Class<TE> getTableEntityClass() {
        return tableEntityClass;
    }

    public Class<PK> getPrimaryKeyDataType() {
        return primaryKeyDataType;
    }

    public String getTableName() {
        return tableName;
    }
}