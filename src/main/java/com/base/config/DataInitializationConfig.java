/**
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
package com.base.config;

import com.base.config.core.authentication.service.DataInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author YISivlay
 */
@Configuration
@EnableTransactionManagement
public class DataInitializationConfig {

    private final DataInitializationService dataInitializationService;

    @Autowired
    public DataInitializationConfig(DataInitializationService dataInitializationService) {
        this.dataInitializationService = dataInitializationService;
    }

    @Bean
    public CommandLineRunner dataInitialization() {
        return args -> {
            dataInitializationService.dataInitialization();
        };
    }

}
