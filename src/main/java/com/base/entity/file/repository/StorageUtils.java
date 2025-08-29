package com.base.entity.file.repository;


import com.base.entity.file.model.StorageType;

import java.io.InputStream;

/**
 * @author YISivlay
 */
public interface StorageUtils {

    StorageType getStorageType();

    String writeFile(final InputStream inputStream,
                     final Long entityId,
                     final String entityName,
                     final String imageName,
                     final StorageRepository storageRepository);

    String readBase64(String path);

    InputStream readInputStream(String path);

    byte[] readByte(String path);

    String readUrl(String path);

    void deleteFile(String path);

}
