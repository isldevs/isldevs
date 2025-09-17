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
package com.base.portfolio.location.village.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author YISivlay
 */
public class VillageConstants {

    public static final String PERMISSION = "VILLAGE";
    public static final String API_PATH = "/villages";

    public static final String ID = "id";
    public static final String COMMUNE = "communeId";
    public static final String TYPE = "type";
    public static final String NAME_EN = "nameEn";
    public static final String NAME_KM = "nameKm";
    public static final String NAME_ZH = "nameZh";
    public static final String POSTAL_CODE = "postalCode";

    public static final Collection<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(ID, COMMUNE, TYPE, NAME_EN, NAME_KM, NAME_ZH, POSTAL_CODE));
}
