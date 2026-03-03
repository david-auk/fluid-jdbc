package io.github.david.auk.fluid.jdbc.apt;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class TableEntityRulesProcessor extends AbstractProcessor {

    // FQNs (no compile-time dependency on your library)
    private static final String FQN_TABLE_ENTITY =
            "io.github.david.auk.fluid.jdbc.components.tables.TableEntity";

    private static final String FQN_TABLE_NAME =
            "io.github.david.auk.fluid.jdbc.annotations.table.TableName";
    private static final String FQN_TABLE_CONSTRUCTOR =
            "io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor";

    private static final String FQN_PRIMARY_KEY =
            "io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey";
    private static final String FQN_FOREIGN_KEY =
            "io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey";
    private static final String FQN_TABLE_COLUMN =
            "io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn";

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

        TypeElement te = elements.getTypeElement(FQN_TABLE_ENTITY);
        this.tableEntityType = te != null ? te.asType() : null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (tableEntityType == null) return false;

        for (Element root : roundEnv.getRootElements()) {
            if (root.getKind() != ElementKind.CLASS && root.getKind() != ElementKind.RECORD) continue;

            TypeElement type = (TypeElement) root;
            if (types.isAssignable(type.asType(), tableEntityType)) {
                validateEntity(type);
            }
        }
        return false;
    }

    private void validateEntity(TypeElement clazz) {
        // @TableName required
        if (!hasAnnotation(clazz, FQN_TABLE_NAME)) {
            error(clazz, "Entity class %s is missing @TableName", clazz.getQualifiedName());
        }

        // @TableConstructor required on at least one ctor
        if (!hasAnnotatedConstructor(clazz, FQN_TABLE_CONSTRUCTOR)) {
            error(clazz, "Entity class %s must have a constructor annotated @TableConstructor", clazz.getQualifiedName());
        }

        List<VariableElement> pkFields = new ArrayList<>();
        List<ExecutableElement> pkMethods = new ArrayList<>();

        // Scan members
        for (Element enclosed : clazz.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement f = (VariableElement) enclosed;

                if (hasAnnotation(f, FQN_PRIMARY_KEY)) pkFields.add(f);

                // @ForeignKey usage: field type must implement TableEntity
                if (hasAnnotation(f, FQN_FOREIGN_KEY)) {
                    validateForeignKeyField(clazz, f);
                }

                // @TableColumn fields must not be primitive
                if (hasAnnotation(f, FQN_TABLE_COLUMN)) {
                    if (f.asType().getKind().isPrimitive()) {
                        error(f, "Field %s in %s annotated @TableColumn must not be a primitive type",
                                f.getSimpleName(), clazz.getQualifiedName());
                    }
                }
            }

            if (enclosed.getKind() == ElementKind.METHOD) {
                ExecutableElement m = (ExecutableElement) enclosed;
                if (hasAnnotation(m, FQN_PRIMARY_KEY)) {
                    pkMethods.add(m);
                }
            }
        }

        // Record-accessor duplicate guard: ignore pk method if it matches pk field name + return type
        List<ExecutableElement> effectivePkMethods = new ArrayList<>();
        for (ExecutableElement m : pkMethods) {
            if (!isRecordAccessorDuplicateOfPkField(m, pkFields)) {
                effectivePkMethods.add(m);
            }
        }

        // Duplicate PK detection
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

        // If PK is FIELD -> must have @TableColumn
        if (pkFields.size() == 1) {
            VariableElement pkField = pkFields.get(0);
            if (!hasAnnotation(pkField, FQN_TABLE_COLUMN)) {
                error(pkField, "Primary key field %s in %s must be annotated @TableColumn",
                        pkField.getSimpleName(), clazz.getQualifiedName());
            }
        }

        // If PK is METHOD -> must not return primitive
        if (effectivePkMethods.size() == 1) {
            ExecutableElement pkMethod = effectivePkMethods.get(0);
            if (pkMethod.getReturnType().getKind().isPrimitive()) {
                error(pkMethod, "Primary key method %s in %s must not return a primitive type",
                        pkMethod.getSimpleName(), clazz.getQualifiedName());
            }
        }
    }

    private void validateForeignKeyField(TypeElement owner, VariableElement field) {
        TypeMirror fieldType = field.asType();

        if (!(fieldType instanceof DeclaredType)) {
            error(field, "Field %s in %s annotated @ForeignKey must have a declared type implementing TableEntity",
                    field.getSimpleName(), owner.getQualifiedName());
            return;
        }

        if (!types.isAssignable(fieldType, tableEntityType)) {
            error(field, "Field %s in %s annotated @ForeignKey must have a type implementing TableEntity (found: %s)",
                    field.getSimpleName(), owner.getQualifiedName(), fieldType);
        }
    }

    private boolean hasAnnotatedConstructor(TypeElement clazz, String annotationFqn) {
        for (Element e : clazz.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR && hasAnnotation(e, annotationFqn)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnnotation(Element element, String annotationFqn) {
        for (AnnotationMirror m : element.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(annotationFqn)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRecordAccessorDuplicateOfPkField(ExecutableElement method, List<VariableElement> pkFields) {
        if (!method.getParameters().isEmpty()) return false;

        String methodName = method.getSimpleName().toString();
        for (VariableElement f : pkFields) {
            if (f.getSimpleName().toString().equals(methodName)) {
                return types.isSameType(method.getReturnType(), f.asType());
            }
        }
        return false;
    }

    private void error(Element element, String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args), element);
    }
}