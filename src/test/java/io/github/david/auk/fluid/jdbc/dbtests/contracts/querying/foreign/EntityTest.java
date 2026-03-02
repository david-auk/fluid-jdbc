package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying.foreign;


import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EntityTest {

    @Test
    void validateEntityLocal() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityQueryForeign.class));
    }

    @Test
    void validateEntityForeign() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(EntityQueryLocal.class));
    }

}
