package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.crud;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EntityCrudTest {

    @Test
    void validateEntityCrud() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityCrud.class));
    }
}
