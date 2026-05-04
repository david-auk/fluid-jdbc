package io.github.david.auk.fluid.jdbc.dbtests.contracts.postgres.jsonb;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Map;
import java.util.UUID;

@TableName("jsonb_test")
public class EntityJsonb implements TableEntity {
        @PrimaryKey
        @TableColumn
        private final UUID id;

        @TableColumn(columnName = "json_via_json_node")
        private final JsonNode jsonViaJsonNode;

        @TableColumn(columnName = "json_via_map")
        private final Map<String, Object> jsonViaMap;

        @TableConstructor
        public EntityJsonb(UUID id, JsonNode jsonViaJsonNode, Map<String, Object> jsonViaMap) {
            this.id = id;
            this.jsonViaJsonNode = jsonViaJsonNode;
            this.jsonViaMap = jsonViaMap;
        }

        public UUID getId() {
                return id;
        }

        public JsonNode getJsonViaJsonNode() {
                return jsonViaJsonNode;
        }

        public Map<String, Object> getJsonViaMap() {
                return jsonViaMap;
        }
}
