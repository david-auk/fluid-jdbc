package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.querying;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.QueryBuilder;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.SingleValueOperator;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.inheritance.EntityInheritBase;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.inheritance.EntityInheritChild;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface ContractQueryInheritance extends ContractInterface {

    default EntityInheritBase newBase(String name, boolean active, int amount) {
        return new EntityInheritBase(UUID.randomUUID().toString(), active, amount, name);
    }

    default EntityInheritChild newChild(
            EntityInheritBase base,
            Integer valueInt,
            Boolean enabled,
            Integer score,
            String description
    ) {
        return new EntityInheritChild(base, valueInt, enabled, score, description);
    }

    default void populateInheritance(
            Dao<EntityInheritBase, String> baseDao,
            Dao<EntityInheritChild, String> childDao,
            List<EntityInheritBase> baseRows,
            List<EntityInheritChild> childRows
    ) {
        Objects.requireNonNull(baseDao, "baseDao");
        Objects.requireNonNull(childDao, "childDao");
        Objects.requireNonNull(baseRows, "baseRows");
        Objects.requireNonNull(childRows, "childRows");

        for (EntityInheritBase base : baseRows) {
            baseDao.add(base);
        }

        for (EntityInheritChild child : childRows) {
            childDao.add(child);
        }
    }

    @Test
    default void queryingInheritance_startsEmpty() {
        try (
                Dao<EntityInheritBase, String> baseDao = dao(EntityInheritBase.class, String.class);
                Dao<EntityInheritChild, String> childDao = dao(EntityInheritChild.class, String.class)
        ) {
            assertTrue(baseDao.getAll().isEmpty(), "base table should start empty");
            assertTrue(childDao.getAll().isEmpty(), "child table should start empty");
        }
    }

    @Test
    default void queryingInheritance_where_parentName_equals_filtersChildRows() throws NoSuchFieldException {
        try (
                Dao<EntityInheritBase, String> baseDao = dao(EntityInheritBase.class, String.class);
                Dao<EntityInheritChild, String> childDao = dao(EntityInheritChild.class, String.class)
        ) {
            EntityInheritBase alphaBase = newBase("alpha", true, 10);
            EntityInheritBase betaBase = newBase("beta", false, 20);
            EntityInheritBase alphaDuplicateBase = newBase("alpha", true, 30);

            List<EntityInheritChild> children = List.of(
                    newChild(alphaBase, 1, true, 100, "first"),
                    newChild(betaBase, 2, false, 200, "second"),
                    newChild(alphaDuplicateBase, 3, true, 300, "third")
            );

            populateInheritance(baseDao, childDao, List.of(alphaBase, betaBase, alphaDuplicateBase), children);

            List<EntityInheritChild> results = new QueryBuilder<>(childDao)
                    .where(EntityInheritBase.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "alpha")
                    .get();

            assertEquals(2, results.size(), "parent name = alpha should match two child rows");
            assertTrue(
                    results.stream().map(EntityInheritChild::getDescription).toList().containsAll(List.of("first", "third")),
                    "results should include the expected child rows"
            );
        }
    }

    @Test
    default void queryingInheritance_where_parentAndChildFields_combinesFilters() throws NoSuchFieldException {
        try (
                Dao<EntityInheritBase, String> baseDao = dao(EntityInheritBase.class, String.class);
                Dao<EntityInheritChild, String> childDao = dao(EntityInheritChild.class, String.class)
        ) {
            EntityInheritBase alphaLow = newBase("alpha", true, 10);
            EntityInheritBase alphaHigh = newBase("alpha", true, 20);
            EntityInheritBase betaHigh = newBase("beta", true, 30);

            List<EntityInheritChild> children = List.of(
                    newChild(alphaLow, 5, true, 10, "low"),
                    newChild(alphaHigh, 50, true, 20, "high"),
                    newChild(betaHigh, 60, true, 30, "other")
            );

            populateInheritance(baseDao, childDao, List.of(alphaLow, alphaHigh, betaHigh), children);

            List<EntityInheritChild> results = new QueryBuilder<>(childDao)
                    .where(EntityInheritBase.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "alpha")
                    .and(EntityInheritChild.class.getDeclaredField("valueInt"), SingleValueOperator.GREATER_THAN, 10)
                    .get();

            assertEquals(1, results.size(), "parent name=alpha AND child valueInt>10 should match one row");
            assertEquals("high", results.getFirst().getDescription(), "matched child row should be high");
        }
    }

    @Test
    default void queryingInheritance_where_multipleParentFields_combinesFilters() throws NoSuchFieldException {
        try (
                Dao<EntityInheritBase, String> baseDao = dao(EntityInheritBase.class, String.class);
                Dao<EntityInheritChild, String> childDao = dao(EntityInheritChild.class, String.class)
        ) {
            EntityInheritBase activeAlpha = newBase("alpha", true, 10);
            EntityInheritBase inactiveAlpha = newBase("alpha", false, 10);
            EntityInheritBase activeBeta = newBase("beta", true, 10);

            List<EntityInheritChild> children = List.of(
                    newChild(activeAlpha, 1, true, 100, "active-alpha"),
                    newChild(inactiveAlpha, 2, true, 200, "inactive-alpha"),
                    newChild(activeBeta, 3, true, 300, "active-beta")
            );

            populateInheritance(baseDao, childDao, List.of(activeAlpha, inactiveAlpha, activeBeta), children);

            List<EntityInheritChild> results = new QueryBuilder<>(childDao)
                    .where(EntityInheritBase.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "alpha")
                    .and(EntityInheritBase.class.getDeclaredField("active"), SingleValueOperator.EQUALS, true)
                    .get();

            assertEquals(1, results.size(), "query using two parent fields should match one child row");
            assertEquals("active-alpha", results.getFirst().getDescription(), "matched child row should be active-alpha");
        }
    }

    @Test
    default void queryingInheritance_where_parentName_like_filtersChildRows() throws NoSuchFieldException {
        try (
                Dao<EntityInheritBase, String> baseDao = dao(EntityInheritBase.class, String.class);
                Dao<EntityInheritChild, String> childDao = dao(EntityInheritChild.class, String.class)
        ) {
            EntityInheritBase alphaBase = newBase("alpha", true, 10);
            EntityInheritBase betaBase = newBase("beta", true, 20);

            List<EntityInheritChild> children = List.of(
                    newChild(alphaBase, 1, true, 100, "alpha-row"),
                    newChild(betaBase, 2, true, 200, "beta-row")
            );

            populateInheritance(baseDao, childDao, List.of(alphaBase, betaBase), children);

            List<EntityInheritChild> results = new QueryBuilder<>(childDao)
                    .where(EntityInheritBase.class.getDeclaredField("name"), SingleValueOperator.LIKE, "alp%")
                    .get();

            assertEquals(1, results.size(), "parent name LIKE alp% should match one child row");
            assertEquals("alpha-row", results.getFirst().getDescription(), "matched child row should be alpha-row");
        }
    }
}