package io.github.david.auk.fluid.jdbc.components.tables;

import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class TableUtilsOld {

    public static <T extends TableEntity> String getTableName(Class<T> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        return annotation != null ? annotation.value() : clazz.getSimpleName().toLowerCase();
    }

    /**
     * Returns the primary‐key accessor, which may be either:
     *  - a Field annotated with {@link PrimaryKey}, or
     *  - a zero‐arg Method annotated {@link PrimaryKey}
     *
     * Field-based PK is preferred over method-based PK.
     *
     * Validation rules enforced here:
     *  - At most one @PrimaryKey member may exist (across fields + methods).
     *  - A @PrimaryKey method must be zero-arg.
     *  - If @TableInherits is present, the class must actually extend the declared base.
     */
    // TODO Subdivide logic into smaller methods
    public static <T extends TableEntity> AccessibleObject getPrimaryKeyMember(Class<T> clazz) {
        // 1) If @TableInherits is present, the entity must actually extend the declared base class.
        // This check must run even when the entity declares its own @PrimaryKey.
        Class<?> inheritedBase = null;
        TableInherits inheritsAnn = clazz.getAnnotation(TableInherits.class);
        if (inheritsAnn != null) {
            inheritedBase = inheritsAnn.value();
            if (!inheritedBase.isAssignableFrom(clazz)) {
                throw new IllegalStateException(
                        "@TableInherits(" + inheritedBase.getName() + ") present but " +
                                clazz.getName() + " does not extend " + inheritedBase.getName()
                );
            }
        }

        // 2) collect all declared @PrimaryKey fields
        List<Field> pkFields = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                f.setAccessible(true);
                pkFields.add(f);
            }
        }

        // 3) collect all declared @PrimaryKey methods
        List<Method> pkMethods = new ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(PrimaryKey.class)) {
                if (m.getParameterCount() != 0) {
                    throw new IllegalArgumentException(
                            "@PrimaryKey method must be zero-arg: " + m.getName());
                }
                m.setAccessible(true);
                pkMethods.add(m);
            }
        }

        // 4) reject duplicates (across fields and/or methods)
        int pkCount = pkFields.size() + pkMethods.size();
        if (pkCount > 1) {
            // Special case: Java records can surface the same component annotation on BOTH
            // the underlying private final field and the generated accessor method.
            // If we see exactly 1 PK field + 1 PK method that represent the same record component,
            // treat it as a single PK and prefer the field.
            if (clazz.isRecord() && pkFields.size() == 1 && pkMethods.size() == 1) {
                Field f = pkFields.getFirst();
                Method m = pkMethods.getFirst();
                boolean sameName = m.getName().equals(f.getName());
                boolean sameType = m.getReturnType().equals(f.getType());
                boolean zeroArg = m.getParameterCount() == 0;
                if (sameName && sameType && zeroArg) {
                    return f;
                }
            }

            String fieldNames = pkFields.stream().map(Field::getName).collect(Collectors.joining(", "));
            String methodNames = pkMethods.stream().map(Method::getName).collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "Multiple @PrimaryKey members found in " + clazz.getName() +
                            ". Fields: [" + fieldNames + "] Methods: [" + methodNames + "]"
            );
        }

        // 5) prefer field PK when present
        if (pkFields.size() == 1) {
            return pkFields.getFirst();
        }
        if (pkMethods.size() == 1) {
            return pkMethods.getFirst();
        }

        // 6) Try via parent entity's PK if @TableInherits is present
        if (inheritedBase != null) {
            // If the base type is also a TableEntity, delegate PK resolution to it.
            if (TableEntity.class.isAssignableFrom(inheritedBase)) {
                @SuppressWarnings("unchecked")
                Class<? extends TableEntity> inheritedEntity = (Class<? extends TableEntity>) inheritedBase;
                return getPrimaryKeyMember(inheritedEntity);
            }
        }

        throw new IllegalStateException(
                "No @PrimaryKey field or method found in " + clazz.getName());
    }

    /**
     * Return the Class<?> that the @PrimaryKey says this key is.
     */
    public static <T extends TableEntity> Class<?> getPrimaryKeyType(Class<T> clazz) {
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);
        if (pkMember instanceof Field) {
            return ((Field) pkMember).getType();
        } else {
            return ((Method) pkMember).getReturnType();
        }
    }

    /**
     * Resolve the physical column columnName for the primary key, following inheritance if needed.
     * Only supports field-based primary keys; method-based PKs cannot reliably yield a column columnName.
     */
    public static <T extends TableEntity> String getPrimaryKeyColumnName(Class<T> clazz) {
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);
        if (pkMember instanceof Field f) {
            TableColumn tc = f.getAnnotation(TableColumn.class);
            if (tc != null && !tc.columnName().isEmpty()) {
                return tc.columnName();
            }
            return f.getName();
        }
        throw new UnsupportedOperationException(
                "Method-based @PrimaryKey not supported for column-columnName resolution. Use a field-based PK or provide an explicit mapping.");
    }

    /**
     * Compute/get the actual PK value from an instance,
     * whether it’s stored in a field or computed by a method.
     */
    public static Object getPrimaryKeyValue(TableEntity instance) {
        AccessibleObject pkMember = getPrimaryKeyMember(instance.getClass());
        try {
            if (pkMember instanceof Field) {
                return ((Field) pkMember).get(instance);
            } else {
                return ((Method) pkMember).invoke(instance);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get primary key value", e);
        }
    }

    public static Map<Field, String> mapFieldToColumnNames(Class<?> clazz) {
        Map<Field, String> map = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableColumn.class)) {
                field.setAccessible(true);
                String name = field.getAnnotation(TableColumn.class).columnName();
                String columnName = name.isEmpty() ? field.getName() : name;
                map.put(field, columnName);
            }
        }
        return map;
    }

    /**
     * Return all the fields that should be INSERTed/UPDATEd as non-PK columns.
     * • If the PK is a field, exclude it.
     * • If the PK is a method, include all @TableField fields.
     */
    public static <T extends TableEntity> List<Field> getNonPrimaryKeyFields(Class<T> clazz) {
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(TableColumn.class))
                .filter(f -> {
                    // exclude if PK is that same field
                    return !(pkMember instanceof Field && (pkMember).equals(f));
                })
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    public static String buildInsertQuery(String tableName, Collection<String> columnNames) {
        String columns = String.join(", ", columnNames);
        String placeholders = String.join(", ", Collections.nCopies(columnNames.size(), "?"));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    /**
     * Build an UPDATE statement that:
     *  - sets all non-PK columns
     *  - if PK is a single field, does "WHERE pk = ?"
     *  - if PK is method-based, does "WHERE col1 = ? AND col2 = ? …"
     */
    public static <T extends TableEntity> String buildUpdateQuery(Class<T> clazz) {
        String tableName = getTableName(clazz);
        Map<Field, String> fieldToCol = mapFieldToColumnNames(clazz);

        // 1) figure out which columns go in the WHERE clause
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);
        String whereClause;
        if (pkMember instanceof Field) {
            // single-column PK
            String pkCol = fieldToCol.get(pkMember);
            whereClause = pkCol + " = ?";
        } else {
            // method-based PK → treat *all* @TableField fields as the composite key
            whereClause = fieldToCol.values().stream()
                    .map(col -> col + " = ?")
                    .collect(Collectors.joining(" AND "));
        }

        // 2) build the SET assignments from the non-PK fields
        List<Field> nonPk = getNonPrimaryKeyFields(clazz);
        String assignments = nonPk.stream()
                .map(f -> fieldToCol.get(f) + " = ?")
                .collect(Collectors.joining(", "));

        return String.format(
                "UPDATE %s SET %s WHERE %s",
                tableName, assignments, whereClause
        );
    }


    /**
     * Build an UPDATE statement that updates the primary key value.
     * <p>
     * Only supported for a single-column primary key that is declared as a field annotated with {@link PrimaryKey}.
     * Method-based/computed primary keys are treated as immutable and are not supported here.
     * <p>
     * Resulting SQL:
     *   UPDATE {@code table} SET {@code pkCol} = ? WHERE {@code pkCol} = ?
     */
    public static <T extends TableEntity> String buildUpdatePrimaryKeyQuery(Class<T> clazz) {
        String tableName = getTableName(clazz);

        AccessibleObject pkMember = getPrimaryKeyMember(clazz);
        if (!(pkMember instanceof Field pkField)) {
            throw new UnsupportedOperationException(
                    "Updating the primary key is only supported when @PrimaryKey is declared on a Field (single-column PK). " +
                    "Method-based/computed primary keys are not supported: " + clazz.getName()
            );
        }

        // Resolve PK column columnName (supports inherited PK fields).
        // This method only supports field-based PKs; method-based PKs are rejected above.
        String pkCol = getPrimaryKeyColumnName(clazz);
        if (pkCol == null || pkCol.isBlank()) {
            throw new IllegalStateException(
                    "Primary key field has no resolvable column columnName: " + pkField.getName());
        }

        return String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?",
                tableName, pkCol, pkCol
        );
    }

}
