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

import com.base.portfolio.file.model.StorageType;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author YISivlay
 */
public interface StorageUtils {

  StorageType getStorageType();

  String writeFile(
      final InputStream inputStream,
      final Long entityId,
      final String entityName,
      final String imageName,
      final String oldFileName,
      final StorageRepository storageRepository);

  String readBase64(Path path);

  InputStream readInputStream(Path path);

  byte[] readByte(Path path);

  String readUrl(Path path);

  void deleteFile(Path path);
}
