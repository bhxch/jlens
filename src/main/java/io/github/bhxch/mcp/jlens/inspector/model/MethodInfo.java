package io.github.bhxch.mcp.jlens.inspector.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Information about a method
 */
public class MethodInfo {

    private final String name;
    private final String returnType;
    private final String descriptor;
    private final List<ParameterInfo> parameters;
    private final List<String> exceptions;
    private final int modifiers;
    private final boolean isStatic;
    private final boolean isFinal;
    private final boolean isSynchronized;
    private final boolean isNative;
    private final boolean isAbstract;
    private final boolean isDefault;
    private final boolean isVarArgs;
    private final String signature;
    private final String since;

    public MethodInfo(String name, String returnType, String descriptor,
                     List<ParameterInfo> parameters, List<String> exceptions,
                     int modifiers, boolean isStatic, boolean isFinal,
                     boolean isSynchronized, boolean isNative, boolean isAbstract,
                     boolean isDefault, boolean isVarArgs, String signature, String since) {
        this.name = name;
        this.returnType = returnType;
        this.descriptor = descriptor;
        this.parameters = new ArrayList<>(parameters);
        this.exceptions = new ArrayList<>(exceptions);
        this.modifiers = modifiers;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        this.isSynchronized = isSynchronized;
        this.isNative = isNative;
        this.isAbstract = isAbstract;
        this.isDefault = isDefault;
        this.isVarArgs = isVarArgs;
        this.signature = signature;
        this.since = since;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public boolean isNative() {
        return isNative;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    public String getSignature() {
        return signature;
    }

    public String getSince() {
        return since;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor);
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
               "name='" + name + '\'' +
               ", returnType='" + returnType + '\'' +
               ", parameters=" + parameters.size() +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String returnType;
        private String descriptor;
        private List<ParameterInfo> parameters = new ArrayList<>();
        private List<String> exceptions = new ArrayList<>();
        private int modifiers;
        private boolean isStatic;
        private boolean isFinal;
        private boolean isSynchronized;
        private boolean isNative;
        private boolean isAbstract;
        private boolean isDefault;
        private boolean isVarArgs;
        private String signature;
        private String since;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder descriptor(String descriptor) {
            this.descriptor = descriptor;
            return this;
        }

        public Builder addParameter(ParameterInfo parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public Builder parameters(List<ParameterInfo> parameters) {
            this.parameters = new ArrayList<>(parameters);
            return this;
        }

        public Builder addException(String exception) {
            this.exceptions.add(exception);
            return this;
        }

        public Builder exceptions(List<String> exceptions) {
            this.exceptions = new ArrayList<>(exceptions);
            return this;
        }

        public Builder modifiers(int modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder isStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public Builder isFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }

        public Builder isSynchronized(boolean isSynchronized) {
            this.isSynchronized = isSynchronized;
            return this;
        }

        public Builder isNative(boolean isNative) {
            this.isNative = isNative;
            return this;
        }

        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public Builder isVarArgs(boolean isVarArgs) {
            this.isVarArgs = isVarArgs;
            return this;
        }

        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder since(String since) {
            this.since = since;
            return this;
        }

        public MethodInfo build() {
            return new MethodInfo(name, returnType, descriptor, parameters,
                               exceptions, modifiers, isStatic, isFinal,
                               isSynchronized, isNative, isAbstract, isDefault,
                               isVarArgs, signature, since);
        }
    }
}




