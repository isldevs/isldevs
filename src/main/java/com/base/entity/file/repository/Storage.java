package com.base.entity.file.repository;


import com.base.entity.file.model.StorageType;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class Storage {

    public StorageRepository repository() {
        return new StorageRepository();
    }

    public StorageUtils repository(final StorageType storeType) {
        if (storeType == StorageType.FILE_SYSTEM) {
            return new StorageRepository();
        }
        return null;
    }
}
