package io.github.david.auk.fluid.jdbc.dbtests.testcontainers.postgres;

import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractPostgresJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.dbtests.support.ContainerSpec;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.github.david.auk.fluid.jdbc.dbtests.support.FetchResource.loadTrimmedResource;

@Tag("latest")
class PostgresLatestContainerTest extends AbstractPostgresJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "postgres:latest",
                () -> {
                    String image = loadTrimmedResource("/image-versions/postgres/latest.txt");
                    return new PostgreSQLContainer<>(image)
                            .withDatabaseName("testdb")
                            .withUsername("test")
                            .withPassword("test");
                }
        );
    }
}