package com.base.config.core.command.data;


/**
 * @author YISivlay
 */
public class Command {

    private Long id;
    private String action;
    private String entity;
    private String permission;
    private String href;

    public Command(Long id, String action, String entity, String href) {
        this.id = id;
        this.action = action;
        this.entity = entity;
        this.permission = action + "_" + entity;
        this.href = href;
    }

}
