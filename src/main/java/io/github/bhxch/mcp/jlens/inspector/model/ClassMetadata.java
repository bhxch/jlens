package io.github.bhxch.mcp.jlens.inspector.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Metadata about a Java class
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.ANY)
public class ClassMetadata {

    private final String className;
    private final String packageName;
    private final String simpleClassName;
    private final String superClass;
    private final List<String> interfaces;
    private final List<FieldInfo> fields;
    private final List<MethodInfo> methods;
    private final List<MethodInfo> constructors;
    
    private final boolean isInterface;
    private final boolean isEnum;
    private final boolean isAnnotation;
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isStatic;
    private final int modifiers;
    private final String sourceFile;
    private final String decompiledSource;
    private final String status;
    private final String suggestion;

    private ClassMetadata(Builder builder) {
        this.className = builder.className;
        this.packageName = builder.packageName;
        this.simpleClassName = builder.simpleClassName;
        this.superClass = builder.superClass;
        this.interfaces = new ArrayList<>(builder.interfaces);
        this.fields = new ArrayList<>(builder.fields);
        this.methods = new ArrayList<>(builder.methods);
        this.constructors = new ArrayList<>(builder.constructors);
        this.isInterface = builder.isInterface;
        this.isEnum = builder.isEnum;
        this.isAnnotation = builder.isAnnotation;
        this.isAbstract = builder.isAbstract;
        this.isFinal = builder.isFinal;
        this.isStatic = builder.isStatic;
        this.modifiers = builder.modifiers;
        this.sourceFile = builder.sourceFile;
        this.decompiledSource = builder.decompiledSource;
        this.status = builder.status != null ? builder.status : "SUCCESS";
        this.suggestion = builder.suggestion;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public String getSuperClass() {
        return superClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public List<MethodInfo> getConstructors() {
        return constructors;
    }

    @JsonProperty("isInterface")
    public boolean isInterface() {
        return isInterface;
    }

    @JsonProperty("isEnum")
    public boolean isEnum() {
        return isEnum;
    }

    @JsonProperty("isAnnotation")
    public boolean isAnnotation() {
        return isAnnotation;
    }

    @JsonProperty("isAbstract")
    public boolean isAbstract() {
        return isAbstract;
    }

    @JsonProperty("isFinal")
    public boolean isFinal() {
        return isFinal;
    }

    @JsonProperty("isStatic")
    public boolean isStatic() {
        return isStatic;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getDecompiledSource() {
        return decompiledSource;
    }

    public String getStatus() {
        return status;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMetadata that = (ClassMetadata) o;
        return Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    @Override
    public String toString() {
        return "ClassMetadata{" +
               "className='" + className + '\'' +
               ", packageName='" + packageName + '\'' +
               ", isInterface=" + isInterface +
               ", isEnum=" + isEnum +
               ", methods=" + methods.size() +
               ", fields=" + fields.size() +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String className;
        private String packageName;
        private String simpleClassName;
        private String superClass = "java.lang.Object";
        private List<String> interfaces = new ArrayList<>();
        private List<FieldInfo> fields = new ArrayList<>();
        private List<MethodInfo> methods = new ArrayList<>();
        private List<MethodInfo> constructors = new ArrayList<>();
        private boolean isInterface;
        private boolean isEnum;
        private boolean isAnnotation;
        private boolean isAbstract;
        private boolean isFinal;
        private boolean isStatic;
        private int modifiers;
        private String sourceFile;
        private String decompiledSource;
        private String status;
        private String suggestion;

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder simpleClassName(String simpleClassName) {
            this.simpleClassName = simpleClassName;
            return this;
        }

        public Builder superClass(String superClass) {
            this.superClass = superClass;
            return this;
        }

        public Builder addInterface(String interfaceName) {
            this.interfaces.add(interfaceName);
            return this;
        }

        public Builder interfaces(List<String> interfaces) {
            this.interfaces = new ArrayList<>(interfaces);
            return this;
        }

        public Builder addField(FieldInfo field) {
            this.fields.add(field);
            return this;
        }

        public Builder fields(List<FieldInfo> fields) {
            this.fields = new ArrayList<>(fields);
            return this;
        }

        public Builder addMethod(MethodInfo method) {
            this.methods.add(method);
            return this;
        }

        public Builder methods(List<MethodInfo> methods) {
            this.methods = new ArrayList<>(methods);
            return this;
        }

        public Builder addConstructor(MethodInfo constructor) {
            this.constructors.add(constructor);
            return this;
        }

        public Builder constructors(List<MethodInfo> constructors) {
            this.constructors = new ArrayList<>(constructors);
            return this;
        }

        public Builder isInterface(boolean isInterface) {
            this.isInterface = isInterface;
            return this;
        }

        public Builder isEnum(boolean isEnum) {
            this.isEnum = isEnum;
            return this;
        }

        public Builder isAnnotation(boolean isAnnotation) {
            this.isAnnotation = isAnnotation;
            return this;
        }

        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }

        public Builder isFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }

        public Builder isStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public Builder modifiers(int modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public Builder decompiledSource(String decompiledSource) {
            this.decompiledSource = decompiledSource;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public ClassMetadata build() {
            return new ClassMetadata(this);
        }
    }
}




