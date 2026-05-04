package io.github.david.auk.fluid.jdbc.dbtests.support;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractMysqlJdbcContainerTest extends AbstractJdbcContainerTest {

    @Override
    protected String[] dropAllTablesSql() {
        return concat(
                super.dropAllTablesSql(),
                new String[]{
                        "DROP TABLE IF EXISTS query_enum_test_table"
                }
        );
    }

    @Override
    protected String[] createAllTablesSql() {
        return concat(
                super.createAllTablesSql(),
                new String[]{
                        """
                        CREATE TABLE query_enum_test_table (
                            id VARCHAR(36) PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            status ENUM('draft', 'active', 'to_archive', 'deleted') NOT NULL,
                            nullable_status ENUM('draft', 'active', 'to_archive', 'deleted')
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