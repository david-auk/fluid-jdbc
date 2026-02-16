package io.github.david.auk.fluid.jdbc.testcontainers.mysql;

import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.support.ContainerSpec;
import org.testcontainers.containers.MySQLContainer;

class MySqlPinnedContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "mysql:pinned",
                () -> new MySQLContainer<>("mysql:8.4") // TODO Move to resources
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test")
        );
    }
}