package io.github.david.auk.fluid.jdbc.support;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

public interface ContractRunner<TE extends TableEntity> {
    String name();
    Class<TE> entityType();
    void run(AbstractJdbcContainerTest env, TestScenario<TE> scenario) throws Exception;
}