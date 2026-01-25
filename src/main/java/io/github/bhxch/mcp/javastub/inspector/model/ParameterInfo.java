package io.github.bhxch.mcp.javastub.inspector.model;

import java.util.Objects;

/**
 * Information about a method parameter
 */
public class ParameterInfo {

    private final String name;
    private final String type;
    private final int index;
    private final boolean isVarArgs;

    public ParameterInfo(String name, String type, int index, boolean isVarArgs) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.isVarArgs = isVarArgs;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterInfo that = (ParameterInfo) o;
        return index == that.index &&
               Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, type);
    }

    @Override
    public String toString() {
        return "ParameterInfo{" +
               "name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", index=" + index +
               ", isVarArgs=" + isVarArgs +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String type;
        private int index;
        private boolean isVarArgs;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder isVarArgs(boolean isVarArgs) {
            this.isVarArgs = isVarArgs;
            return this;
        }

        public ParameterInfo build() {
            return new ParameterInfo(name, type, index, isVarArgs);
        }
    }
}