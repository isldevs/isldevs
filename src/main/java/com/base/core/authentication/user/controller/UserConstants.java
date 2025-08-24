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
package com.base.core.authentication.user.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author YISivlay
 */
public class UserConstants {

    public static final String PERMISSION = "USER";
    public static final String API_PATH = "/users";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String ROLES = "roles";
    public static final String ENABLED = "enabled";
    public static final String IS_ACCOUNT_NON_EXPIRED = "isAccountNonExpired";
    public static final String IS_ACCOUNT_NON_LOCKED = "isAccountNonLocked";
    public static final String IS_CREDENTIALS_NON_EXPIRED = "isCredentialsNonExpired";

    public static final Collection<String> SUPPORTED_PARAMETER = new HashSet<>(Arrays.asList(
            USERNAME,
            PASSWORD,
            NAME,
            EMAIL,
            ROLES,
            ENABLED,
            IS_ACCOUNT_NON_EXPIRED,
            IS_ACCOUNT_NON_LOCKED,
            IS_CREDENTIALS_NON_EXPIRED
    ));
}
