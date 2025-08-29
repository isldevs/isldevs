package com.base.entity.file.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author YISivlay
 */
public class FileConstants {
    public static final String PERMISSION = "FILE";

    public static final Integer MAX_SIZE_IN_MB = 50;
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
