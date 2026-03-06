package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.david.auk.fluid.jdbc.components.tables.utils.InheritanceResolver.getParentPrimaryKeyMemberOrNull;

class PrimaryKeyMemberResolver {

    private PrimaryKeyMemberResolver() {}

    static AccessibleObject getPrimaryKeyMember(Class<? extends TableEntity> tableEntityClass) {

        // Check if AccessiblePrimaryKeyObject can be retrieved via Inheritance
        AccessibleObject AccessiblePrimaryKeyObjectViaInheritance = getParentPrimaryKeyMemberOrNull(tableEntityClass);
        if (AccessiblePrimaryKeyObjectViaInheritance != null) return AccessiblePrimaryKeyObjectViaInheritance;

        return getDeclaredPrimaryKeyMember(tableEntityClass);
    }

    /**
     * Compute/get the actual PK value from an instance,
     * whether it’s stored in a field or computed by a method.
     */
    public static Object getPrimaryKeyValue(Class<? extends TableEntity> tableEntityClass) {
        AccessibleObject pkMember = getPrimaryKeyMember(tableEntityClass);
        try {
            if (pkMember instanceof Field) {
                return ((Field) pkMember).get(tableEntityClass);
            } else {
                return ((Method) pkMember).invoke(tableEntityClass);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get primary key value", e);
        }
    }

    private static AccessibleObject getDeclaredPrimaryKeyMember(
            Class<? extends TableEntity> clazz
    ) {
        List<Field> pkFields = getPrimaryKeyFields(clazz);
        List<Method> pkMethods = getPrimaryKeyMethods(clazz);

        int pkCount = pkFields.size() + pkMethods.size();
        if (pkCount == 0) {
            throw new IllegalStateException(
                    "TableEntity validation did not catch missing @PrimaryKey for "
                            + clazz.getName()
            );
        }

        if (pkCount == 1) {
            if (!pkFields.isEmpty()) {
                return pkFields.getFirst();
            }
            return pkMethods.getFirst();
        }

        return handleDuplicatePrimaryKeys(clazz, pkFields, pkMethods);
    }

    private static List<Field> getPrimaryKeyFields(
            Class<? extends TableEntity> clazz
    ) {
        List<Field> pkFields = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                f.setAccessible(true);
                pkFields.add(f);
            }
        }
        return pkFields;
    }

    private static List<Method> getPrimaryKeyMethods(
            Class<? extends TableEntity> clazz
    ) {
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
        return pkMethods;
    }

    private static AccessibleObject handleDuplicatePrimaryKeys(
            Class<? extends TableEntity> clazz,
            List<Field> pkFields,
            List<Method> pkMethods
    ) {
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
                "TableEntity validation did not catch multiple @PrimaryKey members in " + clazz.getName() +
                        ". Fields: [" + fieldNames + "] Methods: [" + methodNames + "]"
        );
    }
}