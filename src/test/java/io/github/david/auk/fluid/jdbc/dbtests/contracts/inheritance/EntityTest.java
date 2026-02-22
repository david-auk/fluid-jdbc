package io.github.david.auk.fluid.jdbc.dbtests.contracts.inheritance;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EntityTest {

    @Test
    void validateEntityInheritBase() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityInheritBase.class));
    }

    @Test
    void validateEntityInheritChild() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityInheritChild.class));
    }
}
