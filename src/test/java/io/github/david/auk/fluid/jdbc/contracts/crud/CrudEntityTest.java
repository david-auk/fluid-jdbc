package io.github.david.auk.fluid.jdbc.contracts.crud;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CrudEntityTest {

    @Test
    void validateTestCrudEntity() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(CrudEntity.class));
    }
}
