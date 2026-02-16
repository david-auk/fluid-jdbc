package io.github.david.auk.fluid.jdbc.contracts;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;

public interface ContractInterface {

    String createTableSql();
    String dropTableSql();

    DAO<?, ?> dao();
}