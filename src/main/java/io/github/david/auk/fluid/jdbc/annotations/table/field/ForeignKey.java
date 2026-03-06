package io.github.david.auk.fluid.jdbc.annotations.table.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    /**
     * Overwrites the target column from PrimaryKey to a custom column (must be annotated with {@link UniqueColumn}
     * @return The foreign unique column name
     */
    String overwriteJoinColumnWithUniqueColumnName() default "";            // Defaults to PrimaryKey column
}
