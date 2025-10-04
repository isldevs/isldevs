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
package com.base.core.authentication.user.dto;

import com.base.core.authentication.user.controller.UserConstants;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.security.oauth2.core.oidc.StandardClaimAccessor;
import org.springframework.util.Assert;

/**
 * @author YISivlay
 */
public class UserInfoData implements StandardClaimAccessor, Serializable {

    @Serial
    private static final long serialVersionUID = 620L;

    private final Map<String, Object> claims;

    public UserInfoData(Map<String, Object> claims) {
        Assert.notEmpty(claims,
                        "claims cannot be empty");
        this.claims = Collections.unmodifiableMap(new LinkedHashMap<>(claims));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<String, Object> claims = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder id(Long id) {
            return this.claim(UserConstants.ID,
                              id);
        }

        public Builder username(String username) {
            return this.claim(UserConstants.USERNAME,
                              username);
        }

        public Builder name(String name) {
            return this.claim(UserConstants.NAME,
                              name);
        }

        public Builder email(String email) {
            return this.claim(UserConstants.EMAIL,
                              email);
        }

        public Builder roles(Set<String> roles) {
            return this.claim(UserConstants.ROLES,
                              roles);
        }

        public Builder authorities(Set<String> authorities) {
            return this.claim(UserConstants.AUTHORITIES,
                              authorities);
        }

        public Builder enabled(boolean enabled) {
            return this.claim(UserConstants.ENABLED,
                              enabled);
        }

        public Builder authenticated(boolean authenticated) {
            return this.claim(UserConstants.AUTHENTICATED,
                              authenticated);
        }

        public Builder isAccountNonExpired(boolean isAccountNonExpired) {
            return this.claim(UserConstants.IS_ACCOUNT_NON_EXPIRED,
                              isAccountNonExpired);
        }

        public Builder isAccountNonLocked(boolean isAccountNonLocked) {
            return this.claim(UserConstants.IS_ACCOUNT_NON_LOCKED,
                              isAccountNonLocked);
        }

        public Builder isCredentialsNonExpired(boolean isCredentialsNonExpired) {
            return this.claim(UserConstants.IS_CREDENTIALS_NON_EXPIRED,
                              isCredentialsNonExpired);
        }

        public Builder claim(String name,
                             Object value) {
            this.claims.put(name,
                            value);
            return this;
        }

        public Builder claims(Consumer<Map<String, Object>> claimsConsumer) {
            claimsConsumer.accept(this.claims);
            return this;
        }

        public UserInfoData build() {
            return new UserInfoData(this.claims);
        }

    }

    public Map<String, Object> getClaims() { return this.claims; }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            UserInfoData that = (UserInfoData) obj;
            return this.getClaims()
                       .equals(that.getClaims());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getClaims()
                   .hashCode();
    }

}
