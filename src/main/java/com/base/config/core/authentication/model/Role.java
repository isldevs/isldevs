package com.base.config.core.authentication.model;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "roles")
public class Role extends AbstractPersistable<Long> {

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Authority> authorities = new HashSet<>();

    @Column(unique = true, nullable = false)
    private String name;

    protected Role() {}

    public Role(Builder builder) {
        this.name = builder.name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        public Role build() {
            return new Role(this);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }
    }

    public String getName() {
        return name;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }
}
