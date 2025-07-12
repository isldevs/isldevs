package com.base.config.core.command.data;


import com.google.gson.JsonElement;

/**
 * @author YISivlay
 */
public final class JsonCommand {

    private Long id;
    private String json;
    private String href;
    private JsonElement jsonElement;

    public JsonCommand(Builder builder) {
        this.id = builder.id;
        this.json = builder.json;
        this.href = builder.href;
        this.jsonElement = builder.jsonElement;
    }

    public static class Builder {

        private Long id;
        private String json;
        private String href;
        private JsonElement jsonElement;

        public JsonCommand build() {
            return new JsonCommand(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        public Builder json(String json) {
            this.json = json;
            return this;
        }
        public Builder href(String href) {
            this.href = href;
            return this;
        }
        public Builder jsonElement(JsonElement jsonElement) {
            this.jsonElement = jsonElement;
            return this;
        }
    }

    public Long getId() {
        return id;
    }

    public String getJson() {
        return json;
    }

    public String getHref() {
        return href;
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }
}
