package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

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
     * Resolve the physical column columnName for the primary key, following inheritance if needed.
     * Delegates to {@link ColumnResolver#getColumnName(TypedField)}.
     */
    public static String getPrimaryKeyColumnName(Class<? extends TableEntity> tableEntityClass) {
        AccessibleObject pkMember = getPrimaryKeyMember(tableEntityClass);
        if (pkMember instanceof Field field) {
            return ColumnResolver.getColumnName(TypedField.of(tableEntityClass, field, field.getType()));
        }
        throw new UnsupportedOperationException(
                "Method-based @PrimaryKey not supported for column-columnName resolution. Use a field-based PK or provide an explicit mapping.");
    }

    public static <TE extends TableEntity> TypedField<TE, ?> getPrimaryKeyTypedField(Class<TE> tableEntityClass) {
        AccessibleObject pkMember = getPrimaryKeyMember(tableEntityClass);

        if (pkMember instanceof Field field) {
            return TypedField.of(tableEntityClass, field, field.getType());
        }

        throw new UnsupportedOperationException(
                "Method-based @PrimaryKey not supported for TypedField resolution. Use a field-based PK or provide an explicit mapping.");
    }
}
