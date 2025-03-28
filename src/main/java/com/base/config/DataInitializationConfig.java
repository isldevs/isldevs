package com.base.config;


import com.base.config.core.service.DataInitializationService;
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

    @Autowired
    private DataInitializationService dataInitializationService;

    @Bean
    public CommandLineRunner dataInitialization() {
        return args -> {
            dataInitializationService.dataInitialization();
        };
    }

}
