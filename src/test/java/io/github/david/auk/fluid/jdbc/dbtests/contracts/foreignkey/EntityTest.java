package io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EntityTest {

    @Test
    void validateEntityLocal() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityLocal.class));
    }

    @Test
    void validateEntityForeign() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityForeign.class));
    }

    @Test
    void validateEntityForeignSecond() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityForeignSecond.class));
    }
}
