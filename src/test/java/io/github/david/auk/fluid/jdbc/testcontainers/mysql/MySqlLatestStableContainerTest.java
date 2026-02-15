package io.github.david.auk.fluid.jdbc.testcontainers.mysql;

import io.github.david.auk.fluid.jdbc.support.AbstractDatabaseContractSuiteTest;

import io.github.david.auk.fluid.jdbc.support.ContainerSpec;
import io.github.david.auk.fluid.jdbc.support.DatabaseTestSupport;
import org.junit.jupiter.api.Tag;

@Tag("latest")
class MySqlLatestStableContainerTest extends AbstractDatabaseContractSuiteTest {

    @Override
    protected ContainerSpec spec() {
        return DatabaseTestSupport.MYSQL_LTS;
    }
}