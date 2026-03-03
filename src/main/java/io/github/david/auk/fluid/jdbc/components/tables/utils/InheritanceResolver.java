package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AccessibleObject;

final class InheritanceResolver {

    private InheritanceResolver() {
    }

    /**
     * @return true if {@code tableEntityClass} declares a parent via {@link TableInherits}.
     *
     * <p>This method is intentionally "safe": if {@link TableInherits#value()} is not a
     * {@link TableEntity} subtype, or if the class does not actually extend the declared
     * parent, an {@link IllegalStateException} is thrown.</p>
     */
    static boolean hasParent(Class<? extends TableEntity> tableEntityClass) {
        return getParentClassOrNull(tableEntityClass) != null;
    }

    /**
     * Returns the validated parent class declared by {@link TableInherits}, or null if no parent is declared.
     *
     * <p>Validation performed:</p>
     * <ul>
     *   <li>{@link TableInherits#value()} must extend {@link TableEntity}</li>
     *   <li>{@code tableEntityClass} must extend the declared parent</li>
     * </ul>
     */
    static Class<? extends TableEntity> getParentClassOrNull(
            Class<? extends TableEntity> tableEntityClass
    ) {
        TableInherits inherits = tableEntityClass.getAnnotation(TableInherits.class);
        if (inherits == null) {
            return null;
        }

        Class<? extends TableEntity> parentClass = getParentClass(inherits);

        // Ensure the entity actually extends the declared base class.
        if (!parentClass.isAssignableFrom(tableEntityClass)) {
            throw new IllegalStateException(
                    "TableEntity validation did not catch " +
                            tableEntityClass.getName() + " must extend " + parentClass.getName() + " as declared by @TableInherits"
            );
        }

        return parentClass;
    }

    @NotNull
    private static Class<? extends TableEntity> getParentClass(TableInherits inherits) {
        Class<?> rawParent = inherits.value();

        if (!TableEntity.class.isAssignableFrom(rawParent)) {
            throw new IllegalStateException(
                    "TableEntity validation did not catch " +
                            "@TableInherits value must extend " + TableEntity.class.getName() + ": " + rawParent.getName()
            );
        }

        // Safe cast without unchecked warnings.
        return rawParent.asSubclass(TableEntity.class);
    }

    /**
     * Convenience method that returns the parent's primary key member (field/constructor/method)
     * as an {@link AccessibleObject}, using your existing primary-key utilities.
     *
     * <p>Returns null when no parent is declared.</p>
     */
    static AccessibleObject getParentPrimaryKeyMemberOrNull(
            Class<? extends TableEntity> tableEntityClass
    ) {
        Class<? extends TableEntity> parentClass = getParentClassOrNull(tableEntityClass);
        if (parentClass == null) {
            return null;
        }

        // Delegates to your existing primary-key reflection logic.
        return PrimaryKeyMemberResolver.getPrimaryKeyMember(parentClass);
    }
}
