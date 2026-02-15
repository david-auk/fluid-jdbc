package io.github.david.auk.fluid.jdbc.testcontainers.postgres;

import io.github.david.auk.fluid.jdbc.support.AbstractDatabaseContractSuiteTest;
import io.github.david.auk.fluid.jdbc.support.ContainerSpec;
import io.github.david.auk.fluid.jdbc.support.DatabaseTestSupport;

class PostgresPinnedContainerTest extends AbstractDatabaseContractSuiteTest {

    @Override
    protected ContainerSpec spec() {
        return DatabaseTestSupport.POSTGRES_PINNED;
    }
}