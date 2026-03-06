package io.github.david.auk.fluid.jdbc.annotations.table.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO Write JAVADOC
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableColumn {
    String columnName() default "";            // Optional override for column columnName
}
