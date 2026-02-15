package io.github.david.auk.fluid.jdbc.support;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.contracts.EntityUtil;

public record TestScenario<TE extends TableEntity>(
        String name,
        Class<TE> entityClass,
        EntityUtil<TE> entityUtil
) {}