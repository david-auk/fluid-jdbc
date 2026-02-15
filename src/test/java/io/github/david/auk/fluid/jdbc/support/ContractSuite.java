package io.github.david.auk.fluid.jdbc.support;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.contracts.crud.CrudContractLogic;
import io.github.david.auk.fluid.jdbc.contracts.crud.CrudEntity;
import io.github.david.auk.fluid.jdbc.contracts.crud.CrudEntityUtil;
import io.github.david.auk.fluid.jdbc.contracts.foreignkey.ForeignKeyContractLogic;
import io.github.david.auk.fluid.jdbc.contracts.foreignkey.ForeignKeyEntity;
import io.github.david.auk.fluid.jdbc.contracts.foreignkey.ForeignKeyEntityUtil;
import io.github.david.auk.fluid.jdbc.contracts.inheritance.InheritanceContractLogic;
import io.github.david.auk.fluid.jdbc.contracts.inheritance.InheritanceEntity;
import io.github.david.auk.fluid.jdbc.contracts.inheritance.InheritanceEntityUtil;

import java.util.List;

public final class ContractSuite {
    private ContractSuite() {}

    // Scenarios (entity + DDL)
    public static List<TestScenario<?>> scenarios() {
        return List.of(
                new TestScenario<>("crud", CrudEntity.class, new CrudEntityUtil()),
                new TestScenario<>("foreignKey", ForeignKeyEntity.class, new ForeignKeyEntityUtil()),
                new TestScenario<>("inheritance", InheritanceEntity.class, new InheritanceEntityUtil())
        );
    }

    // Runners (test logic)
    public static List<ContractRunner<?>> runners() {
        return List.of(
                new ContractRunner<CrudEntity>() {
                    @Override public String name() { return "crud"; }

                    @Override
                    public Class<CrudEntity> entityType() {
                        return CrudEntity.class;
                    }

                    @Override public void run(AbstractJdbcContainerTest env, TestScenario<CrudEntity> scenario) throws Exception {
                        CrudContractLogic.run(env, scenario);
                    }
                },
                new ContractRunner<ForeignKeyEntity>() {
                    @Override public String name() { return "foreignKey"; }

                    @Override
                    public Class<ForeignKeyEntity> entityType() {
                        return ForeignKeyEntity.class;
                    }

                    @Override public void run(AbstractJdbcContainerTest env, TestScenario<ForeignKeyEntity> scenario) {
                        ForeignKeyContractLogic.run(env, scenario);
                    }
                },
                new ContractRunner<InheritanceEntity>() {
                    @Override public String name() { return "inheritance"; }

                    @Override
                    public Class<InheritanceEntity> entityType() {
                        return InheritanceEntity.class;
                    }

                    @Override public void run(AbstractJdbcContainerTest env, TestScenario<InheritanceEntity> scenario) {
                        InheritanceContractLogic.run(env, scenario);
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public static <T extends TableEntity> TestScenario<T> castScenario(TestScenario<?> s) {
        return (TestScenario<T>) s;
    }

    @SuppressWarnings("unchecked")
    public static <T extends TableEntity> ContractRunner<T> castRunner(ContractRunner<?> r) {
        return (ContractRunner<T>) r;
    }
}