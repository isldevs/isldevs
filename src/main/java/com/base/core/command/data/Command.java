/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.core.command.data;

import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author YISivlay
 */
public class Command implements StandardClaimAccessor, Serializable {

    private final Map<String, Object> claims;

    public Command(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Object> claims = new LinkedHashMap<>();

        private Builder() {
        }

        public Command build() {
            return new Command(this.claims);
        }

        public Builder claim(String name, Object value) {
            this.claims.put(name, value);
            return this;
        }

        public Builder id(Long id) {
            return this.claim("id", id);
        }
        public Builder action(String action) {
            return this.claim("action", action);
        }
        public Builder entity(String entity) {
            return this.claim("entity", entity);
        }
        public Builder entityType(String entityType) {
            return this.claim("entityType", entityType);
        }
        public Builder entityId(Long entityId) {
            return this.claim("entityId", entityId);
        }
        public Builder permission(String permission) {
            return this.claim("permission", permission);
        }
        public Builder href(String href) {
            return this.claim("href", href);
        }
        public Builder json(String json) {
            return this.claim("json", json);
        }
        public Builder file(MultipartFile file) {
            return this.claim("file", file);
        }
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            Command c = (Command) obj;
            return this.getClaims().equals(c.getClaims());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getClaims().hashCode();
    }

    public Long getId() {
        return (Long) claims.get("id");
    }

    public String getAction() {
        return (String) claims.get("action");
    }

    public String getEntity() {
        return (String) claims.get("entity");
    }

    public String getEntityType() {
        return (String) claims.get("entityType");
    }

    public Long getEntityId() {
        return (Long) claims.get("entityId");
    }

    public String getPermission() {
        return (String) claims.get("permission");
    }

    public String getHref() {
        return (String) claims.get("href");
    }

    public String getJson() {
        return (String) claims.get("json");
    }

    public MultipartFile getFile() {
        return (MultipartFile) claims.get("file");
    }

}
