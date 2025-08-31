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
package com.base.entity.file.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author YISivlay
 */
public class FileConstants {
    public static final String PERMISSION = "FILE";
    public static final String API_PATH = "/files";

    public static final Integer MAX_FILE_SIZE = 10;
    public static final Integer MAX_REQUEST_SIZE = 20;
    public static final String DIR = System.getProperty("user.home") + "/" + ".isldevs" + "/";

    public static final String ID = "id";
    public static final String ENTITY = "entity";
    public static final String ENTITY_ID = "entityId";
    public static final String NAME = "name";
    public static final String SIZE = "size";
    public static final String MIMETYPE = "mimeType";
    public static final String LOCATION = "location";
    public static final String STORAGE_TYPE = "storageType";
    public static final String URL = "url";

    public static final Collection<String> SUPPORTED_PARAMETER = new HashSet<>(Arrays.asList(ENTITY,ENTITY_ID));
}
