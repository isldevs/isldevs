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
package com.base.core.exception;

import org.springframework.http.HttpStatus;

/**
 * @author YISivlay
 */
/**
 * @author YISivlay
 */
public class ErrorException extends RuntimeException {

    private final Object[] args;
    private final HttpStatus status;
    private final String description;

    public ErrorException(String msgCode, Object... args) {
        super(msgCode);
        this.status = HttpStatus.BAD_REQUEST;
        this.args = args;
        this.description = null;
    }

    public ErrorException(HttpStatus status, String msgCode, Object... args) {
        super(msgCode);
        this.status = status;
        this.args = args;
        this.description = null;
    }

    public ErrorException(HttpStatus status, String msgCode, String description, Object... args) {
        super(msgCode);
        this.status = status;
        this.args = args;
        this.description = description;
    }

    public Object[] getArgs() {
        return args;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}

