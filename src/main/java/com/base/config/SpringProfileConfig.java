package com.base.config;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author YISivlay
 */
@Component
public class SpringProfileConfig implements EnvironmentAware {

    private ConfigurableEnvironment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = (ConfigurableEnvironment) environment;
        loadEnvFile();
    }

    private void loadEnvFile() {
        String envFile = env.getProperty("spring.env.file");
        String profile = env.getProperty("spring.profiles.active");
        if (envFile != null) {
            try {
                File file = new File(envFile);
                if (file.exists()) {
                    Properties props = new Properties();
                    props.load(new FileInputStream(file));
                    props.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load ." + profile + " file", e);
            }
        }
    }
}
