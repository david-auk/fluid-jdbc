
package io.github.david.auk.fluid.jdbc.dbtests.contracts.postgres.jsonb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface ContractJsonb extends ContractInterface {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    default void jsonb_startsEmpty() {
        try (Dao<EntityJsonb, UUID> dao = dao(EntityJsonb.class, UUID.class)) {
            assertTrue(dao.getAll().isEmpty(), "table should start empty");
        }
    }

    @Test
    default void jsonb_add_insertsRecord() {
        UUID id = UUID.randomUUID();
        JsonNode jsonNode = OBJECT_MAPPER.createObjectNode()
                .put("source", "jsonNode")
                .put("value", 1);
        Map<String, Object> jsonMap = Map.of(
                "source", "map",
                "value", 2
        );

        try (Dao<EntityJsonb, UUID> dao = dao(EntityJsonb.class, UUID.class)) {
            dao.add(new EntityJsonb(id, jsonNode, jsonMap));

            assertEquals(1, dao.getAll().size(), "insert should add exactly one row");
            assertTrue(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

            EntityJsonb read = dao.get(id);
            assertNotNull(read, "dao.get(...) should return a ResultEntity (possibly empty), not null");
            assertEquals(id, read.getId(), "inserted UUID should be readable");
            assertEquals("jsonNode", read.getJsonViaJsonNode().get("source").asText(), "JsonNode string value should round-trip");
            assertEquals(1, read.getJsonViaJsonNode().get("value").asInt(), "JsonNode integer value should round-trip");
            assertEquals("map", read.getJsonViaMap().get("source"), "Map string value should round-trip");
            assertEquals(2, ((Number) read.getJsonViaMap().get("value")).intValue(), "Map integer value should round-trip");
        }
    }

    @Test
    default void jsonb_update_updatesJsonValues() {
        UUID id = UUID.randomUUID();
        EntityJsonb initial = new EntityJsonb(
                id,
                OBJECT_MAPPER.createObjectNode()
                        .put("version", 1)
                        .put("status", "initial"),
                Map.of(
                        "version", 1,
                        "status", "initial"
                )
        );
        EntityJsonb updated = new EntityJsonb(
                id,
                OBJECT_MAPPER.createObjectNode()
                        .put("version", 2)
                        .put("status", "updated"),
                Map.of(
                        "version", 2,
                        "status", "updated"
                )
        );

        try (Dao<EntityJsonb, UUID> dao = dao(EntityJsonb.class, UUID.class)) {
            dao.add(initial);
            dao.update(updated);

            EntityJsonb after = dao.get(id);
            assertEquals(2, after.getJsonViaJsonNode().get("version").asInt(), "updated JsonNode integer value should be readable");
            assertEquals("updated", after.getJsonViaJsonNode().get("status").asText(), "updated JsonNode string value should be readable");
            assertEquals(2, ((Number) after.getJsonViaMap().get("version")).intValue(), "updated Map integer value should be readable");
            assertEquals("updated", after.getJsonViaMap().get("status"), "updated Map string value should be readable");
        }
    }

    @Test
    default void jsonb_nestedJson_roundTrips() {
        UUID id = UUID.randomUUID();
        JsonNode jsonNode = OBJECT_MAPPER.createObjectNode()
                .put("name", "nested")
                .set("child", OBJECT_MAPPER.createObjectNode()
                        .put("enabled", true)
                        .put("count", 3));
        Map<String, Object> jsonMap = Map.of(
                "name", "nested",
                "child", Map.of(
                        "enabled", true,
                        "count", 3
                )
        );

        try (Dao<EntityJsonb, UUID> dao = dao(EntityJsonb.class, UUID.class)) {
            dao.add(new EntityJsonb(id, jsonNode, jsonMap));

            EntityJsonb read = dao.get(id);
            assertTrue(read.getJsonViaJsonNode().get("child").get("enabled").asBoolean(), "nested JsonNode boolean value should round-trip");
            assertEquals(3, read.getJsonViaJsonNode().get("child").get("count").asInt(), "nested JsonNode integer value should round-trip");

            Map<?, ?> child = (Map<?, ?>) read.getJsonViaMap().get("child");
            assertEquals(true, child.get("enabled"), "nested Map boolean value should round-trip");
            assertEquals(3, ((Number) child.get("count")).intValue(), "nested Map integer value should round-trip");
        }
    }

    @Test
    default void jsonb_delete_removesRecord() {
        UUID id = UUID.randomUUID();

        try (Dao<EntityJsonb, UUID> dao = dao(EntityJsonb.class, UUID.class)) {
            dao.add(new EntityJsonb(
                    id,
                    OBJECT_MAPPER.createObjectNode().put("delete", true),
                    Map.of("delete", true)
            ));

            dao.delete(id);

            assertFalse(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be false after delete");
            assertTrue(dao.getAll().isEmpty(), "table should be empty after delete");
        }
    }
}
