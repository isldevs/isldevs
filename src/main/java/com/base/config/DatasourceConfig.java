package com.base.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

    private final Environment env;

    public DatasourceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(env.getRequiredProperty("DB_URL"));
        config.setUsername(env.getRequiredProperty("DB_USERNAME"));
        config.setPassword(env.getRequiredProperty("DB_PASSWORD"));
        config.setDriverClassName(env.getRequiredProperty("DB_DRIVER_CLASS"));

        config.setMaximumPoolSize(Integer.parseInt(env.getProperty("DB_POOL_MAX_SIZE",String.valueOf(Math.min((Runtime.getRuntime().availableProcessors() * 2) + 1, 30)))));
        config.setMinimumIdle(Integer.parseInt(env.getProperty("DB_POOL_MIN_IDLE", String.valueOf(config.getMaximumPoolSize() / 2))));
        config.setConnectionTimeout(Long.parseLong(env.getProperty("DB_CONNECTION_TIMEOUT", "5000"))); // 5s
        config.setIdleTimeout(Long.parseLong(env.getProperty("DB_IDLE_TIMEOUT", "120000"))); // 2m
        config.setMaxLifetime(Long.parseLong(env.getProperty("DB_MAX_LIFETIME", "1800000"))); // 30m
        config.setLeakDetectionThreshold(Long.parseLong(env.getProperty("DB_LEAK_DETECTION_THRESHOLD", "60000"))); // 60s

        if (config.getDriverClassName().contains("postgresql")) {
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("socketTimeout", "30"); // 30 seconds
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
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.base");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("HIBERNATE_DDL_AUTO", "validate"));
        properties.setProperty("hibernate.show_sql", env.getProperty("HIBERNATE_SHOW_SQL", "false"));
        properties.setProperty("hibernate.format_sql", env.getProperty("HIBERNATE_FORMAT_SQL", "true"));
        return properties;
    }

}
