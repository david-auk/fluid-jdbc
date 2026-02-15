package io.github.david.auk.fluid.jdbc.support;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractDatabaseContractSuiteTest extends AbstractJdbcContainerTest {

    @TestFactory
    Stream<DynamicTest> contractSuite() {
        var dbName = spec().displayName();

        return ContractSuite.runners().stream().flatMap(r0 ->
                ContractSuite.scenarios().stream()
                        .filter(s0 -> r0.entityType().equals(s0.entityClass()))
                        .map(s0 -> dynamicTest(
                                "[" + dbName + "] " + r0.name() + " / " + s0.name(),
                                () -> runOne(r0, s0)
                        ))
        );
    }

    private <TE extends TableEntity> void runOne(ContractRunner<TE> runner, TestScenario<?> rawScenario) throws Exception {
        @SuppressWarnings("unchecked")
        TestScenario<TE> scenario = (TestScenario<TE>) rawScenario;

        assertDoesNotThrow(() -> TableEntity.validateEntity(scenario.entityClass()));
        runner.run(this, scenario);
    }
}