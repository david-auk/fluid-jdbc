package io.github.david.auk.fluid.jdbc.contracts.crud;

import io.github.david.auk.fluid.jdbc.contracts.EntityUtil;

public class CrudEntityUtil implements EntityUtil<CrudEntity> {
    @Override
    public String createTableSql() {
        return """
                CREATE TABLE crud_test_table (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    value_int INTEGER NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """;
    }

    @Override
    public String dropTableSql() {
        return "DROP TABLE IF EXISTS crud_test_table";
    }
}
