package io.github.david.auk.fluid.jdbc.dbtests.testcontainers.mysql;

import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.dbtests.support.ContainerSpec;
import org.testcontainers.containers.MySQLContainer;

import static io.github.david.auk.fluid.jdbc.dbtests.support.FetchResource.loadTrimmedResource;

class MySqlPinnedContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "mysql:pinned",
                () -> {
                    String image = loadTrimmedResource("/mysql-pinned-image.txt");
                    return new MySQLContainer<>(image)
                            .withDatabaseName("testdb")
                            .withUsername("test")
                            .withPassword("test");
                }
        );

    }
}