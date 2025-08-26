package com.base.entity.office.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author YISivlay
 */
public class OfficeConstants {
    public static final String API_PATH = "/offices";
    public static final String PERMISSION = "OFFICE";

    public static final String PARENT_ID = "parentId";
    public static final String HIERARCHY = "hierarchy";
    public static final String DECORATED = "decorated";
    public static final String NAME_EN = "nameEn";
    public static final String NAME_KM = "nameKm";
    public static final String NAME_ZH = "nameZh";

    public static final Collection<String> SUPPORTED_PARAMETER = new HashSet<>(Arrays.asList(
            PARENT_ID,
            HIERARCHY,
            DECORATED,
            NAME_EN,
            NAME_KM,
            NAME_ZH
    ));
}
