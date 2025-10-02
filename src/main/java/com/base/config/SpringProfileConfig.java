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
package com.base.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
    var envFile = env.getProperty("spring.env.file");
    var profile = env.getProperty("spring.profiles.active");
    if (envFile != null) {
      try {
        var file = new File(envFile);
        if (file.exists()) {
          var props = new Properties();
          props.load(new FileInputStream(file));
          props.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to load ." + profile + " file", e);
      }
    }
  }
}
