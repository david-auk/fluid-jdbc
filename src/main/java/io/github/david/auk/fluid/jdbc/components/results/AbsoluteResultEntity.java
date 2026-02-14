package io.github.david.auk.fluid.jdbc.components.results;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

public final class AbsoluteResultEntity<T extends TableEntity> extends ResultEntity<T> {

    public AbsoluteResultEntity(T entity) {
        super(entity);
    }

    @Override
    protected ResultType getResultType() {
        return ResultType.ABSOLUTE;
    }
}