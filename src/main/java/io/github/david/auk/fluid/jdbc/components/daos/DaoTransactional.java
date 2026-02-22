package io.github.david.auk.fluid.jdbc.components.daos;

import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.sql.Connection;
import java.sql.SQLException;

/*

Example use:
```java
try (Dao<MyEntity, UUID> dao = new Dao<>(myTable)) {

    dao.add(entityA);
    dao.update(entityB);
    dao.delete(entityCId);

    // any cross-table operations here...

    dao.commit(); // <- ensures your deferred constraints evaluate at COMMIT
} catch (Exception e) {
    // Optional: if you want to be explicit
    // dao.rollback();
    throw e;
}
```

 */
public class DaoTransactional<TE extends TableEntity, PK> extends Dao<TE, PK> {

    public DaoTransactional(Table<TE, PK> table) throws SQLException {
        super(table);
        // We opened the connection in super(table); manage transactions here
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
    }

    public DaoTransactional(Connection connection, Table<TE, PK> table) throws SQLException {
        super(connection, table);
        // External connection: caller owns transaction boundaries
        if (connection.getAutoCommit()) {
            throw new RuntimeException("Connection Auto commit prohibited. please make sure you've disabled auto commit using connection.setAutoCommit(false)");
        }
    }

    /**
     * Commit the current transaction.
     */
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Roll back the current transaction.
     */
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        super.close();
    }
}
