package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.querying.enums;

import io.github.david.auk.fluid.jdbc.annotations.enums.EnumFormat;

//@EnumFormat(db = EnumFormat.Strategy.lower_snake_case, local = EnumFormat.Strategy.UPPER_SNAKE_CASE)
public enum QueryingStatus {
    DRAFT,
    ACTIVE,
    TO_ARCHIVE,
    DELETED
}