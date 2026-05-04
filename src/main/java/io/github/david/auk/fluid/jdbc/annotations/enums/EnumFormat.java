package io.github.david.auk.fluid.jdbc.annotations.enums;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnumFormat {

    /**
     * Format used in the database (external representation)
     */
    Strategy db();

    /**
     * Format used by the enum internally (default: NAME)
     */
    Strategy local() default Strategy.NAME;

    enum Strategy {
        NAME,              // DEFAULT → enum.name()
        LOWERCASE,         // draft
        UPPERCASE,         // DRAFT
        lower_snake_case,  // to_archive
        UPPER_SNAKE_CASE   // TO_ARCHIVE
    }
}