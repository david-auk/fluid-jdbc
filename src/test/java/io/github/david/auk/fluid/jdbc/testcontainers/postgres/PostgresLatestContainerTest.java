package io.github.david.auk.fluid.jdbc.testcontainers.postgres;

import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.support.ContainerSpec;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.PostgreSQLContainer;

@Tag("latest")
class PostgresLatestContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "postgres:latest",
                () -> new PostgreSQLContainer<>("postgres:latest")
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test")
        );
    }
}