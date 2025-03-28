package com.base.config.core.authentication.model;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "authorities")
public class Authority extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private String authority;

    protected Authority() {
    }

    public Authority(Builder builder) {
        this.role = builder.role;
        this.authority = builder.authority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Role role;
        private String authority;

        public Authority build() {
            return new Authority(this);
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }
    }

    public Role getRole() {
        return role;
    }

    public String getAuthority() {
        return authority;
    }
}
