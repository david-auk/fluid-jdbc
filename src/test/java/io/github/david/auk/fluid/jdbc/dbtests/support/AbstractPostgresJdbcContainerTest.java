package io.github.david.auk.fluid.jdbc.dbtests.support;

import io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.querying.enums.ContractEnumQuerying;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.postgres.jsonb.ContractJsonb;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractPostgresJdbcContainerTest extends AbstractJdbcContainerTest implements

        ContractJsonb {

        @Override
        protected String[] dropAllTablesSql() {
                return concat(
                        super.dropAllTablesSql(),
                        new String[]{
                                "DROP TABLE IF EXISTS query_enum_test_table",
                                "DROP TYPE IF EXISTS querying_status",
                                "DROP TABLE IF EXISTS jsonb_test"
                        }
                );
        }

        @Override
        protected String[] createAllTablesSql() {
                return concat(
                        super.createAllTablesSql(),
                        new String[]{
                                """
                        CREATE TYPE querying_status AS ENUM (
                            'DRAFT',
                            'ACTIVE',
                            'TO_ARCHIVE',
                            'DELETED'
                        )
                        """,
                                """
                        CREATE TABLE query_enum_test_table (
                            id VARCHAR(36) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            status querying_status NOT NULL,
                            nullable_status querying_status
                        )
                        """,
                                """
                        CREATE TABLE jsonb_test (
                            id UUID PRIMARY KEY,
                            json_via_json_node JSONB NOT NULL,
                            json_via_map JSONB NOT NULL
                        )
                        """
                        }
                );
        }

        private static String[] concat(String[] first, String[] second) {
                String[] result = new String[first.length + second.length];
                System.arraycopy(first, 0, result, 0, first.length);
                System.arraycopy(second, 0, result, first.length, second.length);
                return result;
        }
}