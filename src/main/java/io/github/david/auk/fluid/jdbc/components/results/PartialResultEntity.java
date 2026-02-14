package io.github.david.auk.fluid.jdbc.components.results;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.ArrayList;

public final class PartialResultEntity<T extends TableEntity> extends ResultEntity<T>{
    private PartialResultEntity(T entity) {
        super(entity);
    }

    @Override
    protected ResultType getResultType() {
        return ResultType.PARTIAL;
    }
}
