package io.github.david.auk.fluid.jdbc.factories;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.DaoTransactional;
import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.sql.Connection;
import java.sql.SQLException;

public class DAOFactory {

    /**
     * Factory method to automatically create new Dao's relying on the Dao to open a new connection.
     * @param clazz The class extending TableEntity that can be converted to a Table and Dao
     * @return An initialized Dao
     * @param <T> TableEntity extending class type
     * @param <K> The PrimaryKey class type
     */
    public static <T extends TableEntity, K> Dao<T, K> createDAO(Class<T> clazz) {
        Table<T, K> table = TableRegistry.getTable(clazz);
        return new Dao<>(table);
    }

    public static  <TE extends TableEntity, PK> DaoTransactional<TE, PK> createTransactionalDAO(Class<TE> clazz) throws SQLException {
        Table<TE, PK> table = TableRegistry.getTable(clazz);
        return new DaoTransactional<>(table);
    }

    /**
     * Factory method to automatically create new Dao's reusing an existing open connection
     * @param connection The open connection object that will be reused for the Dao
     * @param clazz The class extending TableEntity that can be converted to a Table and Dao
     * @return An initialized Dao
     * @param <T> TableEntity extending class type
     * @param <K> The PrimaryKey class type
     */
    public static <T extends TableEntity, K> Dao<T, K> createDAO(Connection connection, Class<T> clazz) {
        Table<T, K> table = TableRegistry.getTable(clazz);
        return new Dao<>(connection, table);
    }

    public static <T extends TableEntity, K> DaoTransactional<T, K> createTransactionalDAO(Connection connection, Class<T> clazz) throws SQLException {
        Table<T, K> table = TableRegistry.getTable(clazz);
        return new DaoTransactional<>(connection, table);
    }
}

