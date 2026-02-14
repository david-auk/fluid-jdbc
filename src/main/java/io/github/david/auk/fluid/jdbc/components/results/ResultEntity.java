package io.github.david.auk.fluid.jdbc.components.results;

import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.Nullable;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public sealed abstract class ResultEntity<T extends TableEntity> permits AbsoluteResultEntity, PartialResultEntity {

    private final ArrayList<Attribute> attributes;
    protected T entity = null;

    protected ResultEntity(ArrayList<Attribute> attributes) {
        this.attributes = attributes;

        assertAllForeignKeysAreFromSameType();
        switch (getResultType()) {
            case ABSOLUTE -> assertAllRequiredKeys();
            case PARTIAL -> {}

            // Cath unimplemented types
            default -> throw new NoSuchElementException();
        }
    }

    protected ResultEntity(T entity) {
        this(Attribute.buildAttributeListFromEntity(entity));
        ResultEntity.this.entity = entity;
    }

    protected abstract ResultType getResultType();


    // TODO move these methods below to separate utils class

    private void assertAllRequiredKeys() throws NoSuchElementException {
        for (Attribute attribute : attributes) {
            boolean isNullable = attribute.hasAnnotation(Nullable.class);

            if (!isNullable && attribute.value == null) {
                throw new NoSuchElementException("No value present for " + attribute.name + " Class: " + attribute.type);
            }
        }
    }

    private void assertAllForeignKeysAreFromSameType() {
        for (Attribute attribute : attributes) {
            boolean isForeignKey = attribute.hasAnnotation(ForeignKey.class);

            if (isForeignKey) {
                if (!(attribute.value instanceof ResultEntity<?> fk)) {
                    throw new IllegalStateException("Foreign key value is not a ResultEntity: " + attribute.name);
                }

                if (fk.getClass() != this.getClass()) {
                    throw new IllegalStateException(
                            "Foreign key ResultEntity wrapper " + fk.getClass() +
                                    " is not of type " + this.getClass()
                    );
                }
            }
        }
    }

    public T getEntity() {

        if (entity == null) {
            throw new IllegalStateException("Entity is null");
        }

        return entity;
    }

    /**
     * Returns true if this result actually contains a fully materialized entity.
     * For ABSOLUTE results this should always be true; for PARTIAL it may be false
     * until the entity is resolved.
     */
    public boolean isPresent() {
        return entity != null;
    }

    /**
     * Returns the entity if present, otherwise throws.
     *
     * Mirrors Optional.require()/get() semantics and is the primary safe accessor
     * for callers that expect a resolved entity.
     */
    public T require() {
        if (entity == null) {
            throw new NoSuchElementException("No entity present in ResultEntity");
        }
        return entity;
    }
}