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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author YISivlay
 */
@Configuration
public class VirtualThreadConfig {

  final Logger logger = LoggerFactory.getLogger(VirtualThreadConfig.class);

  @Bean(value = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
  public Executor applicationTaskExecutor() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    logger.info(
        "Virtual thread per task executor, available processors: {}, will scale dynamically base on workload",
        availableProcessors);
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean
  public TomcatProtocolHandlerCustomizer<?> tomcatVirtualThreadExecutor() {
    logger.info("Tomcat will handle requests with virtual thread per task model");
    return protocolHandler ->
        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
  }
}
