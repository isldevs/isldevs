package com.base.config.core.data;


/**
 * @author YISivlay
 */
public final class ErrorData {

    private final Integer status;
    private final String error;
    private final String description;
    private final Object[] args;

    public ErrorData(Builder builder) {
        this.status = builder.status;
        this.error = builder.error;
        this.description = builder.description;
        this.args = builder.args;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer status;
        private String error;
        private String description;
        private Object[] args;

        public ErrorData build() {
            return new ErrorData(this);
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder args(Object... args) {
            this.args = args;
            return this;
        }
    }

    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public Object[] getArgs() {
        return args;
    }
}
