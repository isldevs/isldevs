package com.base.config.core.command.data;


import java.io.Serializable;
import java.util.Map;

/**
 * @author YISivlay
 */
public class CommandHandlerData implements Serializable {

    private Long id;
    private Map<String, Object> changes;

    public CommandHandlerData(Builder builder) {
        this.id = builder.id;
        this.changes = builder.changes;
    }

    public static class Builder {

        private Long id;
        private Map<String, Object> changes;

        public CommandHandlerData build() {
            return new CommandHandlerData(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder changes(Map<String, Object> changes) {
            this.changes = changes;
            return this;
        }
    }

}
