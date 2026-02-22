package io.github.david.auk.fluid.jdbc.components.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Table<T extends TableEntity, K> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Class<T> clazz;
    private final AccessibleObject primaryKeyMember;
    private final Class<K> primaryKeyDataType;
    private final Map<Field, String> fieldToColumnName;
    private final List<Field> nonPkFields;
    private final List<Field> pkFields;
    // Convenience maps for insert binding
    private final Map<String, Field> columnToField;
    private final List<String> insertColumns;

    protected final String tableName;
    protected final String insertQuery;
    protected final String updateQuery;
    protected final String updatePrimaryKeyQuery;

    @SuppressWarnings("unchecked")
    public Table(Class<T> clazz) {
        this.clazz = clazz;
        this.tableName = TableUtils.getTableName(clazz);

        // map fields → column names
        this.fieldToColumnName = TableUtils.mapFieldToColumnNames(clazz);

        // invert map: column name -> field (used for insert binding)
        this.columnToField = new HashMap<>();
        for (Map.Entry<Field, String> e : fieldToColumnName.entrySet()) {
            this.columnToField.put(e.getValue(), e.getKey());
        }

        // pick up the @PrimaryKey (field or zero-arg method)
        this.primaryKeyMember = TableUtils.getPrimaryKeyMember(clazz);
        this.primaryKeyDataType = (Class<K>) TableUtils.getPrimaryKeyType(clazz);

        // figure out which Fields back the PK for SQL binding
        if (primaryKeyMember instanceof Field) {
            this.pkFields = Collections.singletonList((Field) primaryKeyMember);
        } else {
            // composite: every @TableField column is part of the PK
            this.pkFields = new ArrayList<>(fieldToColumnName.keySet());
        }

        // fields to SET in UPDATE
        this.nonPkFields = fieldToColumnName.keySet().stream()
                .filter(f -> !pkFields.contains(f))
                .collect(Collectors.toList());

        // Compute insert column list. If this entity uses joined-table inheritance and does not
        // declare a local PK field, include the parent's PK column so we can mirror its value.
        this.insertColumns = new ArrayList<>(fieldToColumnName.values());
        boolean inherits = isInheritanceEntity(clazz);
        if (inherits) {
            try {
                String pkCol = getPrimaryKeyColumnName();
                if (pkCol != null && !columnToField.containsKey(pkCol)) {
                    // Prepend PK so it binds first; order only needs to match prepareInsertStatement
                    insertColumns.add(0, pkCol);
                }
            } catch (UnsupportedOperationException ignored) {
                // composite/method-based PK not supported in this convenience path
            }
        }

        this.insertQuery = TableUtils.buildInsertQuery(
                tableName,
                insertColumns
        );
        this.updateQuery = TableUtils.buildUpdateQuery(clazz);

        // optional: only available for single-column field-based PKs
        this.updatePrimaryKeyQuery = (this.primaryKeyMember instanceof Field)
                ? TableUtils.buildUpdatePrimaryKeyQuery(clazz)
                : null;
    }

    // ——————————————————————————————————————————————————————————
    //  Query builder
    // ——————————————————————————————————————————————————————————

    /**
     * Build a SELECT … WHERE … [AND …] [ORDER BY …] query,
     * using the given filter list and sort.
     *
     * @param filters      list of FilterCriterion; null-valued criteria are skipped
     * @param orderByField optional Field to ORDER BY
     * @param ascending    true for ASC, false for DESC
     * @return the SQL string with “?” placeholders for each non-null value
     * @throws IllegalArgumentException if any Field isn’t part of this table’s entity
     */
    public String buildGetQuery(
            List<FilterCriterion<?>> filters,
            Field orderByField,
            boolean ascending
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ")
                .append(tableName);

        boolean first = true;
        for (FilterCriterion<?> criterion : filters) {
            if (appendFilterPredicate(sql, criterion, first)) {
                first = false;
            }
        }

        // append ORDER BY if requested
        if (orderByField != null) {
            if (!orderByField.getDeclaringClass().equals(clazz)) {
                throw new IllegalArgumentException(
                        "Order-by field " + orderByField.getName() +
                                " not from " + clazz.getName());
            }
            String orderCol = fieldToColumnName.get(orderByField);
            if (orderCol == null) {
                throw new IllegalArgumentException(
                        "Missing order-by field " + orderByField.getName());
            }
            sql.append(" ORDER BY ")
                    .append(orderCol)
                    .append(ascending ? " ASC" : " DESC");
        }

        return sql.toString();
    }

    public void prepareInsertStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int idx = 1;

            Object parentPkValue = null;
            String parentPkColumn = null;
            boolean inherits = isInheritanceEntity(clazz);
            if (inherits) {
                try {
                    parentPkColumn = getPrimaryKeyColumnName();
                    parentPkValue = TableUtils.getPrimaryKeyValue(entity);
                } catch (UnsupportedOperationException ignored) {
                    // composite/method-based PK not handled here
                }
            }

            for (String column : insertColumns) {
                Field field = columnToField.get(column);
                if (field == null) {
                    // Not a local field — assume it's the mirrored parent PK column
                    if (inherits && parentPkColumn != null && parentPkColumn.equals(column)) {
                        ps.setObject(idx++, parentPkValue);
                        continue;
                    }
                    throw new IllegalStateException(
                            "No field mapped for column '" + column + "' in entity '" + clazz.getSimpleName() + "'.");
                }

                field.setAccessible(true);
                if (field.isAnnotationPresent(ForeignKey.class)) {
                    Object refObj = field.get(entity);
                    Object fkValue = (refObj == null) ? null : TableUtils.getPrimaryKeyValue(refObj);
                    ps.setObject(idx++, fkValue);
                } else {
                    Object value = field.get(entity);
                    if (value instanceof Map) {
                        String json = objectMapper.writeValueAsString(value);
                        ps.setObject(idx++, json, Types.OTHER);
                    } else {
                        ps.setObject(idx++, value);
                    }
                }
            }
        } catch (IllegalAccessException | JsonProcessingException e) {
            throw new RuntimeException("Failed to access or serialize field", e);
        }
    }

    public void prepareUpdateStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int idx = 1;
            // 1) SET clauses
            for (Field field : nonPkFields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ForeignKey.class)) {
                    Object refObj = field.get(entity);
                    Object fkValue = (refObj == null) ? null : TableUtils.getPrimaryKeyValue(refObj);
                    ps.setObject(idx++, fkValue);
                } else {
                    Object value = field.get(entity);
                    if (value instanceof Map) {
                        String json = objectMapper.writeValueAsString(value);
                        ps.setObject(idx++, json, Types.OTHER);
                    } else {
                        ps.setObject(idx++, value);
                    }
                }
            }
            // 2) WHERE clauses (all PK fields, in declaration order)
            for (Field pkField : pkFields) {
                pkField.setAccessible(true);
                ps.setObject(idx++, pkField.get(entity));
            }
        } catch (IllegalAccessException | JsonProcessingException e) {
            throw new RuntimeException("Failed to access or serialize field", e);
        }
    }

    /**
     * Prepare a statement for updating the primary key value.
     *
     * Expected SQL shape (see {@link TableUtils#buildUpdatePrimaryKeyQuery(Class)}):
     *   UPDATE <table> SET <pkCol> = ? WHERE <pkCol> = ?
     *
     * @param ps          prepared statement created from {@link #getUpdatePrimaryKeyQuery()}
     * @param newPkValue  the new primary key value (SET)
     * @param oldPkValue  the old primary key value (WHERE)
     */
    public void prepareUpdatePrimaryKeyStatement(PreparedStatement ps, Object newPkValue, Object oldPkValue) throws SQLException {
        if (!(primaryKeyMember instanceof Field)) {
            throw new UnsupportedOperationException(
                    "Primary key update is only supported for field-based single-column PKs on " + clazz.getName());
        }
        ps.setObject(1, newPkValue);
        ps.setObject(2, oldPkValue);
    }

    // ——————————————————————————————————————————————————————————
    //  ResultSet → entity
    // ——————————————————————————————————————————————————————————

    /**
     * Returns true if this entity uses joined-table inheritance.
     *
     * Supported signals:
     *  - class-level {@link TableInherits}
     *  - an inheritance-shaped {@link TableConstructor} (first param is a {@link TableEntity})
     */
    private static boolean isInheritanceEntity(Class<?> clazz) {
        if (clazz.isAnnotationPresent(TableInherits.class)) {
            return true;
        }
        return hasInheritanceConstructor(clazz);
    }

    public T buildFromTableWildcardQuery(Connection connection, ResultSet rs) throws SQLException {
        try {
            // Locate @TableConstructor
            Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> c.isAnnotationPresent(TableConstructor.class))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No @TableConstructor on " + clazz.getName()));
            constructor.setAccessible(true);

            // Child-only @TableColumn fields declared on THIS class (not inherited)
            Field[] childFields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(TableColumn.class))
                    .toArray(Field[]::new);

            Parameter[] params = constructor.getParameters();

            // Detect inheritance-aware constructor purely from signature
            boolean hasParentParam = params.length == childFields.length + 1
                    && TableEntity.class.isAssignableFrom(params[0].getType())
                    && isInheritanceEntity(clazz);


            // Validate arity
            if (!hasParentParam && params.length != childFields.length) {
                throw new RuntimeException(
                        "Constructor parameter count (" + params.length + ") does not match field count (" + childFields.length + ")");
            }

            Object[] args = new Object[params.length];
            int offset = 0;

            // Optionally load parent instance (joined-table inheritance pattern)
            if (hasParentParam) {
                @SuppressWarnings("unchecked")
                Class<? extends TableEntity> parentClass = (Class<? extends TableEntity>) params[0].getType();
                String pkColumn = getPrimaryKeyColumnName();
                Object childPk = rs.getObject(pkColumn, primaryKeyDataType);
                try (Dao<? extends TableEntity, Object> parentDao = DAOFactory.createDAO(connection, parentClass)) {
                    args[0] = parentDao.get(childPk);
                }
                offset = 1;
            }

            // Map child columns → constructor args (works for both modes via `offset`)
            for (int i = 0; i < childFields.length; i++) {
                args[offset + i] = readFieldFromResultSet(connection, rs, childFields[i]);
            }

            return clazz.cast(constructor.newInstance(args));

        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    private Object readFieldFromResultSet(Connection connection, ResultSet rs, Field field) throws Exception {
        String column = fieldToColumnName.get(field);
        Class<?> type = field.getType();
        field.setAccessible(true);

        if (field.isAnnotationPresent(ForeignKey.class)) {
            if (!TableEntity.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Foreign key Class does not extend TableEntity");
            }
            @SuppressWarnings("unchecked")
            Class<? extends TableEntity> refClass = (Class<? extends TableEntity>) type;
            Class<?> pkType = TableUtils.getPrimaryKeyType(refClass);
            Object fkId = rs.getObject(column, pkType);
            return (fkId == null) ? null : loadReference(connection, refClass, fkId);
        }

        if (Map.class.isAssignableFrom(type)) {
            String json = rs.getString(column);
            return (json == null)
                    ? Collections.emptyMap()
                    : objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }

        return rs.getObject(column, type);
    }


    /**
     * Returns the raw PK value
     */
    @SuppressWarnings("unchecked")
    public K getPrimaryKey(T entity) {
        return (K) TableUtils.getPrimaryKeyValue(entity);
    }

    /**
     * If you need the actual column name for a single‐field PK.
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
            if (tc != null && tc.name() != null && !tc.name().isEmpty()) {
                return tc.name();
            }
            return pkField.getName();
        }
        throw new UnsupportedOperationException(
                "Composite primary key — use getPrimaryKeyColumnNames()");
    }

    // ——————————————————————————————————————————————————————————
    //  getters & toString()
    // ——————————————————————————————————————————————————————————

    public String getInsertQuery() {
        return insertQuery;
    }

    public String getUpdateQuery() {
        return updateQuery;
    }

    public String getUpdatePrimaryKeyQuery() {
        if (updatePrimaryKeyQuery == null) {
            throw new UnsupportedOperationException(
                    "Primary key update query not available for this table (method-based/composite PK): " + clazz.getName());
        }
        return updatePrimaryKeyQuery;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * Generic loader for referenced entities.
     *
     * @param refClass the entity class
     * @param key      the primary key value
     * @param <R>      the entity type (must extend TableEntity)
     * @param <P>      the primary key type
     * @return the loaded entity or null if not found
     */
    private <R extends TableEntity, P> R loadReference(Connection connection, Class<R> refClass, P key) {
        try (Dao<R, P> dao = DAOFactory.createDAO(connection, refClass)) {
            return dao.get(key);
        }
    }

    public String getColumnName(Field uniqueField) {
        String columnName = fieldToColumnName.get(uniqueField);
        if (columnName == null) {
            throw new IllegalArgumentException(
                    "Field '" + uniqueField.getName() + "' is not a column in table '" + tableName + "'"
            );
        }
        return columnName;
    }

    @Override
    public String toString() {
        return "Table[" + clazz.getSimpleName() + "]:\n" +
                "  Table name: '" + tableName + "'\n" +
                "  PK member: " + primaryKeyMember + " (" +
                primaryKeyDataType.getSimpleName() + ")\n" +
                "  Columns: " + fieldToColumnName.values() + "\n" +
                "  INSERT: " + insertQuery + "\n" +
                "  UPDATE: " + updateQuery + "\n" +
                "  UPDATE PK: " + updatePrimaryKeyQuery + "\n";
    }

    private static boolean hasInheritanceConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(TableConstructor.class))
                .map(Constructor::getParameters)
                .anyMatch(params -> params.length > 0
                        && TableEntity.class.isAssignableFrom(params[0].getType()));
    }

    /**
     * Build a {@code DELETE FROM <table> WHERE ...} query from the given filter list.
     *
     * <ul>
     *   <li>Null-valued criteria are skipped.</li>
     *   <li>Predicates are combined with {@code AND} in the order provided.</li>
     *   <li>Uses {@code LIKE ?} when {@link FilterCriterion#wildcard()} is true, otherwise {@code = ?}.</li>
     * </ul>
     *
     * <p>Safety: if no non-null predicates remain, this throws to prevent accidental full-table deletes.</p>
     */
    public String buildDeleteQuery(List<FilterCriterion<?>> filters) {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName);

        boolean first = true;
        int predCount = 0;

        for (FilterCriterion<?> criterion : filters) {
            if (appendFilterPredicate(sql, criterion, first)) {
                first = false;
                predCount++;
            }
        }

        if (predCount == 0) {
            throw new IllegalArgumentException(
                    "Refusing to build DELETE without any non-null filters for table '" + tableName + "'.");
        }

        return sql.toString();
    }

    private boolean appendFilterPredicate(StringBuilder sql, FilterCriterion<?> criterion, boolean first) {
        Field f = criterion.field();
        Object v = criterion.value();

        // skip null filters
        if (v == null) return false;

        // validate field belongs to this entity
        if (!f.getDeclaringClass().equals(clazz)) {
            throw new IllegalArgumentException(
                    "Field " + f.getName() + " not from " + clazz.getName());
        }

        String col = fieldToColumnName.get(f);
        if (col == null) {
            throw new IllegalArgumentException(
                    "Missing field " + f.getName() + " in table " + tableName);
        }

        sql.append(first ? " WHERE " : " AND ")
                .append(col)
                .append(criterion.wildcard() ? " LIKE ?" : " = ?");

        return true;
    }
}