package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;
import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.EntityRelation;
import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.RelationKind;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public final class TableUtils {
    public static String getTableName(Class<? extends TableEntity> tableEntityClass) {
        return TableInfoResolver.getTableName(tableEntityClass);
    }

    public static AccessibleObject getPrimaryKeyMember(Class<? extends TableEntity> tableEntityClass) {
        return PrimaryKeyMemberResolver.getPrimaryKeyMember(tableEntityClass);
    }

    public static <T extends TableEntity> Object getPrimaryKeyValue(T tableEntity) {
        return PrimaryKeyMemberResolver.getPrimaryKeyValue(tableEntity);
    }

    public static Class<?> getPrimaryKeyType(Class<? extends TableEntity> tableEntityClass) {
        return PrimaryKeyInfoResolver.getPrimaryKeyType(tableEntityClass);
    }

    public static String getPrimaryKeyColumnName(Class<? extends TableEntity> clazz) {
        return PrimaryKeyInfoResolver.getPrimaryKeyColumnName(clazz);
    }

    public static Map<TypedField<? extends TableEntity, ?>, String> mapFieldToColumnNames(Class<? extends TableEntity> tableEntityClass) {
        return FieldMapResolver.mapFieldToColumnNames(tableEntityClass);
    }

    public static String getColumnName(TypedField<? extends TableEntity, ?> field) {
        return ColumnResolver.getColumnName(field);
    }

    public static String getColumnName(Field field) {
        return ColumnResolver.getColumnName(field);
    }

    public static <LC extends TableEntity, FC extends TableEntity> String getColumnNameOfForeignTableEntity(TypedField<LC, FC> localTypedField) {
        return ColumnResolver.getForeignColumnName(localTypedField);
    }

    public static <LC extends TableEntity, FC extends TableEntity> EntityRelation<LC, FC> resolveEntityRelation(
            Class<LC> entityToBeQueried,
            Class<FC> foreignClass
    ) {
        return ColumnResolver.resolveEntityRelation(entityToBeQueried, foreignClass);
    }

    public static Class<? extends TableEntity> getParentTableEntityClass(Class<? extends TableEntity> tableEntityClass) {
        return InheritanceResolver.getParentClassOrNull(tableEntityClass);
    }

    public static <TE extends TableEntity> TypedField<TE, ?> getPrimaryKeyTypedField(Class<TE> tableEntityClass) {
        return PrimaryKeyInfoResolver.getPrimaryKeyTypedField(tableEntityClass);
    }

    public static <TE extends TableEntity> List<TypedField<TE, ?>> getAllColumnFields(Class<TE> clazz) {
        return ColumnResolver.getAllColumnFields(clazz);
    }

    public static <LC extends TableEntity, FC extends TableEntity> Object getForeignColumnValue(LC entity, TypedField<LC, FC> foreignColumn) {
        return ColumnResolver.getForeignColumnValue(entity, foreignColumn);
    }
}
