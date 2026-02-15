package io.github.david.auk.fluid.jdbc.testcontainers.postgres;

import io.github.david.auk.fluid.jdbc.contracts.crud.CrudContract;
import io.github.david.auk.fluid.jdbc.contracts.crud.CrudEntity;
import io.github.david.auk.fluid.jdbc.contracts.crud.CrudEntityUtil;
import io.github.david.auk.fluid.jdbc.support.AbstractDatabaseContractSuiteTest;
import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.support.ContainerSpec;
import io.github.david.auk.fluid.jdbc.support.DatabaseTestSupport;
import org.junit.jupiter.api.Tag;

@Tag("latest")
class PostgresLatestContainerTest extends AbstractDatabaseContractSuiteTest {

    @Override
    protected ContainerSpec spec() {
        return DatabaseTestSupport.POSTGRES_LATEST;
    }
}