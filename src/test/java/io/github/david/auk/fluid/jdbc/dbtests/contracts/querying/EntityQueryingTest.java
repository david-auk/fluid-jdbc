package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EntityQueryingTest {

    @Test
    void validateEntityCrud() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityQuerying.class));
    }
}
