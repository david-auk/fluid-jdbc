package io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;

import java.sql.Connection;
import java.sql.Statement;

public interface ContractForeignKey extends ContractInterface {

//    @Override
//    default void teardownSchema(Connection c) throws Exception {
//        try (Statement s = c.createStatement()) {
//            s.execute("DROP TABLE IF EXISTS local_test_table");
//            s.execute("DROP TABLE IF EXISTS foreign_test_table");
//        }
//    }
//
//    @Override
//    default void setupSchema(Connection c) throws Exception {
//        try (Statement s = c.createStatement()) {
//            s.execute("""
//                CREATE TABLE foreign_test_table (
//                    name  VARCHAR(255) NOT NULL,
//                    value INTEGER      NOT NULL,
//                    CONSTRAINT pk_foreign_test_table PRIMARY KEY (name)
//                )
//            """);
//
//            s.execute("""
//                CREATE TABLE local_test_table (
//                    name                 VARCHAR(255) NOT NULL,
//                    foreign_entity_name  VARCHAR(255) NOT NULL,
//
//                    CONSTRAINT pk_local_test_table PRIMARY KEY (name),
//                    CONSTRAINT fk_local_foreign_entity
//                        FOREIGN KEY (foreign_entity_name)
//                        REFERENCES foreign_test_table (name)
//                        ON UPDATE CASCADE
//                        ON DELETE RESTRICT
//                )
//            """);
//        }
//    }
}