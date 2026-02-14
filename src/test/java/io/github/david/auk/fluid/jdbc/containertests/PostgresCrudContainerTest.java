package io.github.david.auk.fluid.jdbc.containertests;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgresCrudContainerTest extends AbstractCrudContainerTest<PostgreSQLContainer<?>> {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Override
    protected PostgreSQLContainer<?> container() {
        return POSTGRES;
    }
}