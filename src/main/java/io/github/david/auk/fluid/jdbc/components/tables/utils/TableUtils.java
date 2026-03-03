package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import jdk.jshell.spi.ExecutionControl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Map;

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

    public static Map<Field, String> mapFieldToColumnNames(Class<? extends TableEntity> tableEntityClass) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("need to implement");
    }

    public static String getColumnName(Field field) {
        return ColumnResolver.getColumnName(field);
    }
}
