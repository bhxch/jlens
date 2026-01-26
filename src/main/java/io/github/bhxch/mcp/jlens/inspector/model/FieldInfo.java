package io.github.bhxch.mcp.jlens.inspector.model;

import java.util.Objects;

/**
 * Information about a field
 */
public class FieldInfo {

    private final String name;
    private final String type;
    private final String signature;
    private final int modifiers;
    private final boolean isStatic;
    private final boolean isFinal;
    private final boolean isVolatile;
    private final boolean isTransient;
    private final Object constantValue;

    public FieldInfo(String name, String type, String signature, int modifiers,
                    boolean isStatic, boolean isFinal, boolean isVolatile,
                    boolean isTransient, Object constantValue) {
        this.name = name;
        this.type = type;
        this.signature = signature;
        this.modifiers = modifiers;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        this.isVolatile = isVolatile;
        this.isTransient = isTransient;
        this.constantValue = constantValue;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSignature() {
        return signature;
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

    public boolean isVolatile() {
        return isVolatile;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public Object getConstantValue() {
        return constantValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldInfo fieldInfo = (FieldInfo) o;
        return Objects.equals(name, fieldInfo.name) &&
               Objects.equals(type, fieldInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
               "name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", isStatic=" + isStatic +
               ", isFinal=" + isFinal +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String type;
        private String signature;
        private int modifiers;
        private boolean isStatic;
        private boolean isFinal;
        private boolean isVolatile;
        private boolean isTransient;
        private Object constantValue;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder signature(String signature) {
            this.signature = signature;
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

        public Builder isVolatile(boolean isVolatile) {
            this.isVolatile = isVolatile;
            return this;
        }

        public Builder isTransient(boolean isTransient) {
            this.isTransient = isTransient;
            return this;
        }

        public Builder constantValue(Object constantValue) {
            this.constantValue = constantValue;
            return this;
        }

        public FieldInfo build() {
            return new FieldInfo(name, type, signature, modifiers,
                               isStatic, isFinal, isVolatile, isTransient, constantValue);
        }
    }
}




