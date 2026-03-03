package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.github.david.auk.fluid.jdbc.components.tables.utils.PrimaryKeyMemberResolver.getPrimaryKeyMember;

final class PrimaryKeyInfoResolver {

    private PrimaryKeyInfoResolver() {
    }

    static Class<?> getPrimaryKeyType(Class<? extends TableEntity> tableEntityClass) {
        AccessibleObject pkMember = getPrimaryKeyMember(tableEntityClass);
        if (pkMember instanceof Field) {
            return ((Field) pkMember).getType();
        } else {
            return ((Method) pkMember).getReturnType();
        }
    }

    /**
     * Resolve the physical column name for the primary key, following inheritance if needed.
     * Delegates to {@link ColumnResolver#getColumnName(Field)}.
     */
    public static String getPrimaryKeyColumnName(Class<? extends TableEntity> tableEntityClass) {
        AccessibleObject pkMember = getPrimaryKeyMember(tableEntityClass);
        if (pkMember instanceof Field field) {
            return ColumnResolver.getColumnName(field);
        }
        throw new UnsupportedOperationException(
                "TableEntity validation did not catch " +
                "Method-based @PrimaryKey not supported for column-name resolution. Use a field-based PK or provide an explicit mapping.");
    }
}
