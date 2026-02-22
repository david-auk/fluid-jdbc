package io.github.david.auk.fluid.jdbc.annotations.table.constructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableInherits {
    /**
     * The parent entity class that this table inherits from.
     */
    Class<?> value();
}
