package io.github.david.auk.fluid.jdbc.dbtests.testcontainers.postgres;

import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.dbtests.support.ContainerSpec;
import org.testcontainers.containers.PostgreSQLContainer;

class PostgresPinnedContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "postgres:pinned",
                () -> new PostgreSQLContainer<>("postgres:16-alpine") // TODO Move to resources
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test")
        );
    }
}