package com.base.config.core.model;


import jakarta.persistence.*;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    private User user;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(nullable = false)
    private String authority;

    protected Authority() {}

    public Authority(Builder builder) {
        this.user = builder.user;
        this.username = builder.username;
        this.authority = builder.authority;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private User user;
        private String username;
        private String authority;

        public Authority build() {
            return new Authority(this);
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthority() {
        return authority;
    }
}
