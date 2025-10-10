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
package com.base.portfolio.file.model;

import com.base.core.exception.ErrorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author YISivlay
 */
public enum StorageType {

    FILE_SYSTEM("File system", 1), S3("S3", 2), MINIO("MinIO", 3);

    private static final Map<Integer, StorageType> ENUM_MAP = Collections.unmodifiableMap(Arrays.stream(values())
            .collect(Collectors.toMap(StorageType::getValue, type -> type)));

    private final String name;
    private final int value;

    StorageType(String name,
                int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static StorageType fromInt(int i) {
        StorageType type = ENUM_MAP.get(i);
        if (type == null) {
            throw new ErrorException("msg.storage.type.invalid", i);
        }
        return type;
    }

}
