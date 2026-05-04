
package io.github.david.auk.fluid.jdbc.components.tables.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class TableEntityResolver<TE extends TableEntity, PK> {

    private final Class<TE> tableEntityClass;
    private final Class<PK> primaryKeyDataType;
    private final Map<Field, String> fieldToColumnName;
    private final ObjectMapper objectMapper;

    public TableEntityResolver(
            Class<TE> tableEntityClass,
            Class<PK> primaryKeyDataType,
            Map<Field, String> fieldToColumnName,
            ObjectMapper objectMapper
    ) {
        this.tableEntityClass = Objects.requireNonNull(tableEntityClass, "tableEntityClass cannot be null");
        this.primaryKeyDataType = Objects.requireNonNull(primaryKeyDataType, "primaryKeyDataType cannot be null");
        this.fieldToColumnName = Objects.requireNonNull(fieldToColumnName, "fieldToColumnName cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }

    public TE buildFromTableWildcardQuery(Connection connection, ResultSet rs) throws SQLException {
        try {
            Constructor<?> constructor = findTableConstructor();
            Field[] childFields = getDeclaredTableColumnFields();
            Parameter[] parameters = constructor.getParameters();

            boolean hasParentParameter = hasParentParameter(parameters, childFields);
            validateConstructorArity(parameters, childFields, hasParentParameter);

            Object[] constructorArguments = new Object[parameters.length];
            int argumentOffset = 0;

            if (hasParentParameter) {
                constructorArguments[0] = loadParentEntity(connection, rs, parameters[0]);
                argumentOffset = 1;
            }

            for (int fieldIndex = 0; fieldIndex < childFields.length; fieldIndex++) {
                constructorArguments[argumentOffset + fieldIndex] = readFieldFromResultSet(connection, rs, childFields[fieldIndex]);
            }

            return tableEntityClass.cast(constructor.newInstance(constructorArguments));
        }
        catch (Exception e) {
            if (e instanceof SQLException sqlException) {
                throw sqlException;
            }

            throw new RuntimeException("Failed to create instance of " + tableEntityClass.getName(), e);
        }
    }

    private Constructor<?> findTableConstructor() {
        Constructor<?> constructor = Arrays.stream(tableEntityClass.getDeclaredConstructors())
                .filter(candidate -> candidate.isAnnotationPresent(TableConstructor.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No @TableConstructor on " + tableEntityClass.getName()));

        constructor.setAccessible(true);
        return constructor;
    }

    private Field[] getDeclaredTableColumnFields() {
        return Arrays.stream(tableEntityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(TableColumn.class))
                .toArray(Field[]::new);
    }

    private boolean hasParentParameter(Parameter[] parameters, Field[] childFields) {
        return parameters.length == childFields.length + 1
                && TableEntity.class.isAssignableFrom(parameters[0].getType())
                && isInheritanceEntity();
    }

    private void validateConstructorArity(
            Parameter[] parameters,
            Field[] childFields,
            boolean hasParentParameter
    ) {
        if (!hasParentParameter && parameters.length != childFields.length) {
            throw new RuntimeException(
                    "Constructor parameter count (" + parameters.length + ") does not match field count (" + childFields.length + ")"
            );
        }
    }

    private TableEntity loadParentEntity(Connection connection, ResultSet rs, Parameter parentParameter) throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends TableEntity> parentClass = (Class<? extends TableEntity>) parentParameter.getType();

        String primaryKeyColumnName = getPrimaryKeyColumnName();
        Object childPrimaryKeyValue = rs.getObject(primaryKeyColumnName, primaryKeyDataType);

        try (Dao<? extends TableEntity, Object> parentDao = DAOFactory.createDAO(connection, parentClass)) {
            return parentDao.get(childPrimaryKeyValue);
        }
    }

    private Object readFieldFromResultSet(Connection connection, ResultSet rs, Field field) throws Exception {
        String columnName = fieldToColumnName.get(field);
        Class<?> fieldType = field.getType();
        field.setAccessible(true);

        if (field.isAnnotationPresent(ForeignKey.class)) {
            if (!TableEntity.class.isAssignableFrom(fieldType)) {
                throw new IllegalArgumentException("Foreign key Class does not extend TableEntity");
            }

            @SuppressWarnings("unchecked")
            Class<? extends TableEntity> referencedClass = (Class<? extends TableEntity>) fieldType;
            Class<?> referencedPrimaryKeyType = TableUtils.getPrimaryKeyType(referencedClass);
            Object foreignKeyValue = rs.getObject(columnName, referencedPrimaryKeyType);

            return foreignKeyValue == null
                    ? null
                    : loadReference(connection, referencedClass, foreignKeyValue);
        }

        if (Map.class.isAssignableFrom(fieldType)) {
            String json = rs.getString(columnName);
            return json == null
                    ? Collections.emptyMap()
                    : objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        }

        if (fieldType.isEnum()) {
            String dbValue = rs.getString(columnName);

            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;

            // TODO Check if this is the right way to handle enums
            return EnumFormatter.parse(enumType, dbValue);
        }

        return rs.getObject(columnName, fieldType);
    }

    private <R extends TableEntity> R loadReference(
            Connection connection,
            Class<R> referencedClass,
            Object foreignKeyValue
    ) throws Exception {
        try (Dao<R, Object> referencedDao = DAOFactory.createDAO(connection, referencedClass)) {
            return referencedDao.get(foreignKeyValue);
        }
    }

    private boolean isInheritanceEntity() {
        return TableUtils.getParentTableEntityClass(tableEntityClass) != null;
    }

    private String getPrimaryKeyColumnName() {
        return TableUtils.getColumnName(TableUtils.getPrimaryKeyTypedField(tableEntityClass));
    }
}
