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
package com.base.portfolio.file.service;


import com.base.core.command.data.JsonCommand;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

/**
 * @author YISivlay
 */
public interface FileService {

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'UPLOAD_FILE')")
    Map<String, Object> uploadFile(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'DELETE_FILE')")
    Map<String, Object> delete(JsonCommand command);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_FILE')")
    Map<String, Object> fileURL(String entity, Long entityId);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_FILE')")
    Map<String, Object> fileBase64(String entity, Long entityId);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_FILE')")
    Map<String, Object> fileByte(String entity, Long entityId);

    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'READ_FILE')")
    Map<String, Object> fileInputStream(String entity, Long entityId);
}
