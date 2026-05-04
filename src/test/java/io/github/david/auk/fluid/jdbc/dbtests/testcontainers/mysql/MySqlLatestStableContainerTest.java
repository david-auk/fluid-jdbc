package io.github.david.auk.fluid.jdbc.dbtests.testcontainers.mysql;

import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractMysqlJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.dbtests.support.ContainerSpec;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.MySQLContainer;

import static io.github.david.auk.fluid.jdbc.dbtests.support.FetchResource.loadTrimmedResource;

@Tag("latest")
class MySqlLatestStableContainerTest extends AbstractMysqlJdbcContainerTest {

    @Override
    protected ContainerSpec spec() {
        return new ContainerSpec(
                "mysql:lts",
                () -> {
                    String image = loadTrimmedResource("/image-versions/mysql/latest.txt");
                    return new MySQLContainer<>(image)
                            .withDatabaseName("testdb")
                            .withUsername("test")
                            .withPassword("test");
                }
        );
    }
}