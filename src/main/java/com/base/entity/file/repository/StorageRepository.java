package com.base.entity.file.repository;


import com.base.entity.file.controller.FileConstants;
import com.base.entity.file.model.StorageType;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author YISivlay
 */
public class StorageRepository implements StorageUtils {

    @Override
    public StorageType getStorageType() {
        return StorageType.FILE_SYSTEM;
    }

    @Override
    public String writeFile(InputStream inputStream,
                            Long entityId,
                            String entityName,
                            String fileName,
                            StorageRepository storageRepository) {

        Path path = Paths.get(FileConstants.DIR, entityName, String.valueOf(entityId), fileName);

        FileUtils.write(inputStream, path.toAbsolutePath().toString());
        return path.toAbsolutePath().toString();
    }

    @Override
    public String readBase64(String path) {
        return "";
    }

    @Override
    public InputStream readInputStream(String path) {
        return null;
    }

    @Override
    public byte[] readByte(String path) {
        return new byte[0];
    }

    @Override
    public String readUrl(String path) {
        return "";
    }

    @Override
    public void deleteFile(String path) {

    }
}
