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
package com.base.portfolio.file.repository;

import com.base.core.exception.ErrorException;
import com.base.portfolio.file.controller.FileConstants;
import com.base.portfolio.file.model.StorageType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Base64;
import org.springframework.http.HttpStatus;

/**
 * @author YISivlay
 */
public class StorageRepository implements StorageUtils {

    @Override
    public StorageType getStorageType() { return StorageType.FILE_SYSTEM; }

    @Override
    public String writeFile(InputStream inputStream,
                            Long entityId,
                            String entityName,
                            String fileName,
                            String oldFileName,
                            StorageRepository storageRepository) {

        Path dir = Paths.get(FileConstants.DIR,
                             entityName,
                             String.valueOf(entityId));
        Path path = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            if (oldFileName != null && !oldFileName.isBlank()) {
                Path oldPath = dir.resolve(oldFileName);
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                             "msg.internal.error",
                                             "Deleting previous file failed",
                                             oldPath);
                }
            }

            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
                extension = fileName.substring(dotIndex + 1);
            }

            InputStream resizedStream = FileUtils.resize(extension,
                                                         inputStream);
            Files.copy(resizedStream,
                       path,
                       StandardCopyOption.REPLACE_EXISTING);
            return path.toAbsolutePath()
                       .toString();
        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                     "msg.internal.error",
                                     "Writing file failed",
                                     path);
        }
    }

    @Override
    public String readBase64(Path path) {
        try {
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ErrorException(HttpStatus.NOT_FOUND,
                                         "msg.not.found",
                                         path);
            }
            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder()
                         .encodeToString(bytes);
        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                     "msg.internal.error",
                                     "Reading file failed",
                                     path);
        }
    }

    @Override
    public InputStream readInputStream(Path path) {
        try {
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ErrorException(HttpStatus.NOT_FOUND,
                                         "msg.not.found",
                                         path);
            }
            return Files.newInputStream(path,
                                        StandardOpenOption.READ);
        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                     "msg.internal.error",
                                     "Failed to read file as InputStream",
                                     path);
        }
    }

    @Override
    public byte[] readByte(Path path) {
        try {
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ErrorException(HttpStatus.NOT_FOUND,
                                         "msg.not.found",
                                         path);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                     "msg.internal.error",
                                     "Reading file failed",
                                     path);
        }
    }

    @Override
    public String readUrl(Path path) {
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            throw new ErrorException(HttpStatus.NOT_FOUND,
                                     "msg.not.found",
                                     "File does not exist",
                                     path);
        }
        return path.toAbsolutePath()
                   .toString();
    }

    @Override
    public void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                     "msg.internal.error",
                                     "Deleting file failed",
                                     path);
        }
    }

}
