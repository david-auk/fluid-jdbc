package io.github.david.auk.fluid.jdbc.contracts;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.support.TestScenario;

public interface EntityTest<TE extends TableEntity> {
    TestScenario<TE> scenario();
}