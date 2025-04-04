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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author YISivlay
 */
@Configuration
public class DatasourceConfig {

    private final Dotenv env;

    @Autowired
    public DatasourceConfig(Environment environment) {
        var activeProfile = environment.getActiveProfiles()[0];
        this.env = Dotenv.configure()
                .directory("./config")
                .filename("." + activeProfile)
                .ignoreIfMissing()
                .load();
    }


    @Bean
    public DataSource dataSource() {

        var config = new HikariConfig();

        config.setJdbcUrl(env.get("DB_URL"));
        config.setUsername(env.get("DB_USERNAME"));
        config.setPassword(env.get("DB_PASSWORD"));
        config.setDriverClassName(env.get("DB_DRIVER_CLASS"));

        config.setMaximumPoolSize(Integer.parseInt(env.get("DB_POOL_MAX_SIZE", String.valueOf(Math.min((Runtime.getRuntime().availableProcessors() * 2) + 1, 30)))));
        config.setMinimumIdle(Integer.parseInt(env.get("DB_POOL_MIN_IDLE", String.valueOf(config.getMaximumPoolSize() / 2))));
        config.setConnectionTimeout(Long.parseLong(env.get("DB_CONNECTION_TIMEOUT")));
        config.setIdleTimeout(Long.parseLong(env.get("DB_IDLE_TIMEOUT")));
        config.setMaxLifetime(Long.parseLong(env.get("DB_MAX_LIFETIME")));
        config.setLeakDetectionThreshold(Long.parseLong(env.get("DB_LEAK_DETECTION_THRESHOLD")));

        if (config.getDriverClassName().contains("postgresql")) {
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("socketTimeout", "30");
        }

        if (config.getDriverClassName().contains("mysql")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("useServerPrepStmts", "true");
        }

        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.base");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    @Bean
    public JpaProperties jpaProperties() {
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.setOpenInView(false);
        return jpaProperties;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        var transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        var properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", env.get("HIBERNATE_DDL_AUTO"));
        properties.setProperty("hibernate.show_sql", env.get("HIBERNATE_SHOW_SQL"));
        properties.setProperty("hibernate.format_sql", env.get("HIBERNATE_FORMAT_SQL"));
        return properties;
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {

        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineVersion("1")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}
