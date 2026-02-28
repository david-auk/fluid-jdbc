package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.Nullable;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

/**
 * Minimal, cross-db friendly entity used for testing querying features.
 *
 * <p>Intended query coverage:</p>
 * <ul>
 *   <li>Equality lookup: {@code id}, {@code category}</li>
 *   <li>Text queries: {@code name} (LIKE / prefix / contains)</li>
 *   <li>Range queries + ordering: {@code valueInt}</li>
 *   <li>Boolean filtering: {@code enabled}</li>
 * </ul>
 */
@TableName("query_test_table")
public record EntityQuerying(
        @PrimaryKey @TableColumn String id,
        @TableColumn @Nullable String name,
        @TableColumn @Nullable String category,
        @TableColumn(name = "value_int") Integer valueInt,
        @TableColumn Boolean enabled
) implements TableEntity {

    @TableConstructor
    public EntityQuerying(String id, String name, String category, Integer valueInt, Boolean enabled) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = name;
        this.category = category;
        this.valueInt = Objects.requireNonNull(valueInt, "valueInt");
        this.enabled = Objects.requireNonNull(enabled, "enabled");
    }
}
