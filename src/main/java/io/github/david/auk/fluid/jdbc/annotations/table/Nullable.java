package io.github.david.auk.fluid.jdbc.annotations.table;

//import com.mcm.backend.app.api.utils.components.ValidatedBodyResolver;

import java.lang.annotation.*;


/**
 * Indicates that a field is okay to leave as null. Null value is okay or value automatically generated (within constructor),
 * such as primary keys or timestamps.
 * <p>
 * Fields annotated with {@link Nullable} will be skipped if empty during request body binding
 * and will always be passed as {@code null} to the entity constructor.
 * </p>
 *
 * @see io.github.david.auk.fluid.jdbc.annotations.table.PrimaryKey
 * @see TableColumn
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Nullable {
}