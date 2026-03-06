package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;
import jdk.jshell.spi.ExecutionControl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public final class TableUtils {
    public static String getTableName(Class<? extends TableEntity> tableEntityClass) {
        return TableInfoResolver.getTableName(tableEntityClass);
    }

    public static AccessibleObject getPrimaryKeyMember(Class<? extends TableEntity> tableEntityClass) {
        return PrimaryKeyMemberResolver.getPrimaryKeyMember(tableEntityClass);
    }

    public static Object getPrimaryKeyValue(Class<? extends TableEntity> tableEntityClass) {
        return PrimaryKeyMemberResolver.getPrimaryKeyValue(tableEntityClass);
    }

    public static Class<?> getPrimaryKeyType(Class<? extends TableEntity> tableEntityClass) {
        return PrimaryKeyInfoResolver.getPrimaryKeyType(tableEntityClass);
    }

    public static String getPrimaryKeyColumnName(Class<? extends TableEntity> clazz) {
        return PrimaryKeyInfoResolver.getPrimaryKeyColumnName(clazz);
    }

    public static <TE extends TableEntity> Map<TypedField<TE, Object>, String> mapFieldToColumnNames(Class<TE> tableEntityClass) {
        return FieldMapResolver.mapFieldToColumnNames(tableEntityClass);
    }

    public static String getColumnName(TypedField<? extends TableEntity, ?> field) {
        return ColumnResolver.getColumnName(field);
    }

    public static <LC extends TableEntity, FC extends TableEntity> String getColumnNameOfForeignTableEntity(TypedField<LC, ?> typedField,  Class<FC> foreignEntityClass) {
        return ColumnResolver.getForeignColumnName(typedField, foreignEntityClass);
    }

    public static <LC extends TableEntity, FC extends TableEntity> Field getLocalFieldOfTypeForeignEntity(Class<LC> entityToBeQueried, Class<FC> foreignClass) {
        return ColumnResolver.getLocalFieldOfTypeForeignEntity(entityToBeQueried, foreignClass);
    }

    public static Class<? extends TableEntity> getParentTableEntityClass(Class<? extends TableEntity> tableEntityClass) {
        return InheritanceResolver.getParentClassOrNull(tableEntityClass);
    }

    public static <TE extends TableEntity> TypedField<TE, ?> getPrimaryKeyTypedField(Class<TE> tableEntityClass) {
        return PrimaryKeyInfoResolver.getPrimaryKeyTypedField(tableEntityClass);
    }
}
