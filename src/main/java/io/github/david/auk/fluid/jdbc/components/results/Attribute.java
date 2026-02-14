package io.github.david.auk.fluid.jdbc.components.results;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class Attribute {
    public final String name;
    public final Class<?> type;
    public final Object value;
    public final Annotation[] annotations;

    public Attribute(Field field, Object value) {
        this.name = field.getName();
        this.value = value;
        this.type = field.getType();
        this.annotations = field.getAnnotations();
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return Arrays.stream(annotations).anyMatch(a -> a.annotationType() == annotation);
    }

    static ArrayList<Attribute> buildAttributeListFromEntity(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        }

        ArrayList<Attribute> attributes = new ArrayList<>();

        // Walk the class hierarchy so we include fields declared on superclasses as well.
        for (Class<?> current = entity.getClass(); current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                // Ignore synthetic/compiler-generated fields and class-level (static) fields.
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                boolean wasAccessible = field.canAccess(entity);
                if (!wasAccessible) {
                    field.setAccessible(true);
                }

                Object value;
                try {
                    value = field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to read field '" + field.getName() + "' from " + entity.getClass().getName(), e);
                }

                attributes.add(new Attribute(field, value));

                // Best-effort: restore original accessibility.
                if (!wasAccessible) {
                    try {
                        field.setAccessible(false);
                    } catch (Exception ignored) {
                        // Ignored on purpose
                    }
                }
            }
        }

        return attributes;
    }
}
