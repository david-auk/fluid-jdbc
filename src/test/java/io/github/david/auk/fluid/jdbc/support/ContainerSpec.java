package io.github.david.auk.fluid.jdbc.support;

import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.function.Supplier;

public record ContainerSpec(
        String displayName,
        Supplier<? extends JdbcDatabaseContainer<?>> factory
) {}