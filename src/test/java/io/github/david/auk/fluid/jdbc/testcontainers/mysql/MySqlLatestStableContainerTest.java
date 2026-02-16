package io.github.david.auk.fluid.jdbc.testcontainers.mysql;

import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.support.ContainerSpec;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.MySQLContainer;

@Tag("latest")
class MySqlLatestStableContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "mysql:lts",
                () -> new MySQLContainer<>("mysql:lts")
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test")
        );
    }
}