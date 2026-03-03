package io.github.david.auk.fluid.jdbc.apt;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_21) // change to your target
public final class TableEntityRulesProcessor extends AbstractProcessor {

    private Types types;
    private Elements elements;
    private Messager messager;

    private TypeMirror tableEntityType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();

        TypeElement te = elements.getTypeElement(TableEntity.class.getCanonicalName());
        this.tableEntityType = te != null ? te.asType() : null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (tableEntityType == null) {
            // If the TableEntity type isn't on the classpath, we can't validate.
            return false;
        }

        // Validate every class/record that implements TableEntity.
        // We find candidates by scanning root elements (fast enough for typical projects).
        for (Element root : roundEnv.getRootElements()) {
            if (root.getKind() != ElementKind.CLASS && root.getKind() != ElementKind.RECORD) {
                continue;
            }
            TypeElement type = (TypeElement) root;

            if (implementsTableEntity(type)) {
                validateEntity(type);
            }
        }

        return false;
    }

    private boolean implementsTableEntity(TypeElement type) {
        // includes indirect inheritance
        return types.isAssignable(type.asType(), tableEntityType);
    }

    private void validateEntity(TypeElement clazz) {
        // 1) @TableName on class
        if (clazz.getAnnotation(TableName.class) == null) {
            error(clazz, "Entity class %s is missing @TableName", clazz.getQualifiedName());
        }

        // 2) must have constructor annotated @TableConstructor
        if (!hasAnnotatedConstructor(clazz, TableConstructor.class)) {
            error(clazz, "Entity class %s must have a constructor annotated @TableConstructor", clazz.getQualifiedName());
        }

        // Collect members
        List<VariableElement> pkFields = new ArrayList<>();
        List<ExecutableElement> pkMethods = new ArrayList<>();

        Map<String, VariableElement> fieldsByName = new HashMap<>();

        for (Element enclosed : clazz.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement f = (VariableElement) enclosed;
                fieldsByName.put(f.getSimpleName().toString(), f);

                // @ForeignKey type must implement TableEntity
                if (f.getAnnotation(ForeignKey.class) != null) {
                    validateForeignKeyField(clazz, f);
                }

                // @TableColumn must not be primitive
                if (f.getAnnotation(TableColumn.class) != null) {
                    validateNonPrimitiveTableColumnField(clazz, f);
                }

                // PK fields
                if (f.getAnnotation(PrimaryKey.class) != null) {
                    pkFields.add(f);
                }
            } else if (enclosed.getKind() == ElementKind.METHOD) {
                ExecutableElement m = (ExecutableElement) enclosed;

                if (m.getAnnotation(PrimaryKey.class) != null) {
                    pkMethods.add(m);
                }
            }
        }

        // 3) Determine PK member with "field preferred" and record-accessor duplicate guard
        List<ExecutableElement> effectivePkMethods = new ArrayList<>();
        for (ExecutableElement m : pkMethods) {
            if (isRecordAccessorDuplicateOfPkField(m, pkFields)) {
                continue;
            }
            effectivePkMethods.add(m);
        }

        // Reject duplicate PK definitions
        if (pkFields.size() > 1) {
            error(clazz, "Entity class %s has multiple @PrimaryKey fields (%d). Only one is allowed.",
                    clazz.getQualifiedName(), pkFields.size());
        }
        if (effectivePkMethods.size() > 1) {
            error(clazz, "Entity class %s has multiple @PrimaryKey methods (%d). Only one is allowed.",
                    clazz.getQualifiedName(), effectivePkMethods.size());
        }
        if (!pkFields.isEmpty() && !effectivePkMethods.isEmpty()) {
            error(clazz, "Entity class %s has both a @PrimaryKey field and a @PrimaryKey method. Choose one.",
                    clazz.getQualifiedName());
        }
        if (pkFields.isEmpty() && effectivePkMethods.isEmpty()) {
            error(clazz, "Entity class %s is missing @PrimaryKey (field preferred, or method allowed).",
                    clazz.getQualifiedName());
        }

        // 4) If PK is FIELD -> must have @TableColumn
        if (pkFields.size() == 1) {
            VariableElement pkField = pkFields.get(0);
            if (pkField.getAnnotation(TableColumn.class) == null) {
                error(pkField,
                        "Primary key field '%s' in %s must be annotated @TableColumn",
                        pkField.getSimpleName(), clazz.getQualifiedName());
            }
        }

        // 5) If PK is METHOD -> must not return primitive
        if (effectivePkMethods.size() == 1) {
            ExecutableElement pkMethod = effectivePkMethods.get(0);
            if (pkMethod.getReturnType().getKind().isPrimitive()) {
                error(pkMethod,
                        "Primary key method '%s' in %s must not return a primitive type",
                        pkMethod.getSimpleName(), clazz.getQualifiedName());
            }
        }
    }

    private boolean hasAnnotatedConstructor(TypeElement clazz, Class<?> annotationType) {
        for (Element e : clazz.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                if (e.getAnnotationMirrors().stream().anyMatch(m -> m.getAnnotationType().toString()
                        .equals(annotationType.getCanonicalName()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateForeignKeyField(TypeElement owner, VariableElement field) {
        TypeMirror fieldType = field.asType();

        // Must be a declared type (class/record/interface), not primitive/array/etc
        if (!(fieldType instanceof DeclaredType)) {
            error(field,
                    "Field '%s' in %s annotated @ForeignKey must have a declared type implementing TableEntity",
                    field.getSimpleName(), owner.getQualifiedName());
            return;
        }

        if (!types.isAssignable(fieldType, tableEntityType)) {
            error(field,
                    "Field '%s' in %s annotated @ForeignKey must have a type implementing TableEntity (found: %s)",
                    field.getSimpleName(), owner.getQualifiedName(), fieldType);
        }
    }

    private void validateNonPrimitiveTableColumnField(TypeElement owner, VariableElement field) {
        TypeKind kind = field.asType().getKind();
        if (kind.isPrimitive()) {
            error(field,
                    "Field '%s' in %s annotated @TableColumn must not be a primitive type",
                    field.getSimpleName(), owner.getQualifiedName());
        }
    }

    /**
     * If a record-style accessor method is annotated @PrimaryKey AND we also have a PK field
     * with the same name, treat the method as a duplicate accessor and ignore it for PK counting.
     */
    private boolean isRecordAccessorDuplicateOfPkField(ExecutableElement method, List<VariableElement> pkFields) {
        if (!method.getParameters().isEmpty()) return false;

        String methodName = method.getSimpleName().toString();
        for (VariableElement f : pkFields) {
            if (f.getSimpleName().toString().equals(methodName)) {
                // Same name. If return type matches field type, it's very likely a record accessor.
                return types.isSameType(method.getReturnType(), f.asType());
            }
        }
        return false;
    }

    private void error(Element element, String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args), element);
    }
}