package io.github.david.auk.fluid.jdbc.containertests;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MysqlCrudContainerTest extends AbstractCrudContainerTest<MySQLContainer<?>> {

    @Container
    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Override
    protected MySQLContainer<?> container() {
        return MYSQL;
    }

    // Usually not needed if your base DDL is portable.
    // Override createTableSql() here if your dialect needs tweaks.
}