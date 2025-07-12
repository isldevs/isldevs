package com.base.config.core.command.data;


/**
 * @author YISivlay
 */
public class CommandBuilder {

    private Long id;
    private String action;
    private String entity;
    private String href;
    private String json;

    public Command build() {
        return new Command(id, action, entity, href);
    }

    public CommandBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public CommandBuilder action(String action) {
        this.action = action;
        return this;
    }

    public CommandBuilder entity(String entity) {
        this.entity = entity;
        return this;
    }

    public CommandBuilder href(String href) {
        this.href = href;
        return this;
    }
    public CommandBuilder json(String json) {
        this.json = json;
        return this;
    }
}
