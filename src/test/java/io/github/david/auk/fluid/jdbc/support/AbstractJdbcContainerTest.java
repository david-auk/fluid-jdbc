package io.github.david.auk.fluid.jdbc.support;

import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractJdbcContainerTest {

    private JdbcDatabaseContainer<?> container;
    private Connection connection;

    protected abstract ContainerSpec spec();

    @BeforeAll
    void startContainerAndConfigureDatabase() {
        container = spec().factory().get();
        container.start();

        System.setProperty("datasource.url", container.getJdbcUrl());
        System.setProperty("datasource.username", container.getUsername());
        System.setProperty("datasource.password", container.getPassword());
    }

    @AfterAll
    void stopContainer() {
        if (container != null) container.stop();
    }

    @BeforeEach
    void openConnection() {
        connection = Database.getConnection();
    }

    @AfterEach
    void closeConnection() throws Exception {
        if (connection != null) connection.close();
    }

    protected final Connection connection() {
        return connection;
    }

    public final <TE extends TableEntity> DAO<TE, String> prepareDao(TestScenario<TE> scenario) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(scenario.entityUtil().dropTableSql());
            statement.execute(scenario.entityUtil().createTableSql());
        } catch (SQLException e) {
            throw new RuntimeException("Schema init failed for scenario: " + scenario.name(), e);
        }

        return DAOFactory.createDAO(connection, scenario.entityClass());
    }
}