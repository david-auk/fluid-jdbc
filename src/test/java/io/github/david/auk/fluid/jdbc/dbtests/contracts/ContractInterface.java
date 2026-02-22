package io.github.david.auk.fluid.jdbc.dbtests.contracts;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.DaoTransactional;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;

import java.sql.Connection;
import java.sql.SQLException;

public interface ContractInterface {

    /**
     * The implementing test class must also extend AbstractJdbcContainerTest.
     * This is the only “constraint” we need for accessing dao()/connection().
     */
    default AbstractJdbcContainerTest self() {
        if (this instanceof AbstractJdbcContainerTest t) {
            return t;
        }
        throw new IllegalStateException(
                getClass().getName() + " must extend " + AbstractJdbcContainerTest.class.getName()
        );
    }

//    default void setupSchema(Connection connection) throws Exception {
//        // no-op by default; contract can override
//    }
//
//    default void teardownSchema(Connection connection) throws Exception {
//        // no-op by default; contract can override
//    }

    default <TE extends TableEntity, PK> Dao<TE, PK> dao(Class<TE> entityClass, Class<PK> pkClass) {
        return DAOFactory.createDAO(self().connection(), entityClass);
    }

    default <TE extends TableEntity, PK> DaoTransactional<TE, PK> daoTransactional(Class<TE> entityClass, Class<PK> pkClass) {
        try {

            return DAOFactory.createTransactionalDAO(self().connectionTransactional(), entityClass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    default void commit() throws SQLException {
        self().connectionTransactional().commit();
    }
}