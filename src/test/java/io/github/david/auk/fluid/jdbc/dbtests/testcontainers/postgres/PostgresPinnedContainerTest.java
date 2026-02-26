package io.github.david.auk.fluid.jdbc.dbtests.testcontainers.postgres;

import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.dbtests.support.ContainerSpec;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.github.david.auk.fluid.jdbc.dbtests.support.FetchResource.loadTrimmedResource;

class PostgresPinnedContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "postgres:pinned",
                () -> {
                    String image = loadTrimmedResource("/image-versions/postgres/pinned.txt");
                    return new PostgreSQLContainer<>(image)
                            .withDatabaseName("testdb")
                            .withUsername("test")
                            .withPassword("test");
                }
        );
    }
}