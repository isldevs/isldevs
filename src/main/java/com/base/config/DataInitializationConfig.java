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
