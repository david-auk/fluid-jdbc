package io.github.david.auk.fluid.jdbc.support;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public final class DatabaseTestSupport {

    private DatabaseTestSupport() {}

    // Define what “pinned” means. Keep these stable.
    public static final ContainerSpec POSTGRES_PINNED = new ContainerSpec(
            "postgres:pinned",
            () -> new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
    );

    // “latest” means what Testcontainers pulls at runtime.
    public static final ContainerSpec POSTGRES_LATEST = new ContainerSpec(
            "postgres:latest",
            () -> new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
    );

    public static final ContainerSpec MYSQL_PINNED = new ContainerSpec(
            "mysql:pinned",
            () -> new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
    );

    /**
     * "latest" for MySQL is an innovation release and can introduce breaking changes.
     * For compatibility testing, we track the latest LTS release instead.
     *
     * If you also want to test the newest innovation release, add a separate spec (see MYSQL_INNOVATION).
     */
    public static final ContainerSpec MYSQL_LTS = new ContainerSpec(
            "mysql:lts",
            () -> new MySQLContainer<>("mysql:lts")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
    );

    /**
     * Optional helper if you ever need access to the raw container type.
     */
    public static JdbcDatabaseContainer<?> create(ContainerSpec spec) {
        return spec.factory().get();
    }
}