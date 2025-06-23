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
package com.base.config.core.schedule.service;

import com.base.config.core.schedule.model.ScheduleJob;
import com.base.config.core.schedule.model.ScheduledJobHistory;
import com.base.config.core.schedule.repository.ScheduleJobRepository;
import com.base.config.core.schedule.repository.ScheduledJobHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author YISivlay
 */
@Configuration
@EnableScheduling
public class ScheduleJobService implements SchedulingConfigurer {

    private final Logger logger = LoggerFactory.getLogger(ScheduleJobService.class);

    private final ApplicationContext applicationContext;
    private final ScheduleJobRepository jobRepository;
    private final ScheduledJobHistoryRepository jobHistoryRepository;

    public ScheduleJobService(final ApplicationContext applicationContext,
                              final ScheduleJobRepository jobRepository,
                              final ScheduledJobHistoryRepository jobHistoryRepository) {
        this.applicationContext = applicationContext;
        this.jobRepository = jobRepository;
        this.jobHistoryRepository = jobHistoryRepository;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        var jobs = jobRepository.findAll();
        if (!jobs.isEmpty()) {
            for (ScheduleJob job : jobs) {
                if (!job.isEnabled()) continue;
                var task = getRunnableBean(job.getBeanName());
                Trigger trigger = triggerContext -> {
                    var cron = job.getCronExpression();
                    try {
                        if (!CronExpression.isValidExpression(cron)) {
                            throw new IllegalArgumentException("Invalid cron: " + cron);
                        }
                        return new CronTrigger(cron).nextExecution(triggerContext);
                    } catch (Exception ex) {
                        logger.error("Invalid cron expression for job {}: {}. Using fallback.", job.getJobName(), ex.getMessage());
                        return new CronTrigger("0 0 0 */30 * *").nextExecution(triggerContext);
                    }
                };

                Runnable wrappedTask = () -> {
                    var executedAt = Timestamp.valueOf(LocalDateTime.now().withNano(0));
                    Timestamp nextExecuteAt = null;
                    try {
                        var cronTrigger = new CronTrigger(job.getCronExpression());
                        var next = cronTrigger.nextExecution(new SimpleTriggerContext());
                        if (next != null) {
                            var nextLocalDateTime = LocalDateTime.ofInstant(next, ZoneId.systemDefault()).withNano(0);
                            nextExecuteAt = Timestamp.valueOf(nextLocalDateTime);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to calculate nextExecuteAt for job '{}'", job.getJobName(), e);
                    }

                    var history = new ScheduledJobHistory();
                    history.setJobName(job.getJobName());
                    history.setExecutedAt(executedAt);
                    history.setNextExecutedAt(nextExecuteAt);

                    try {
                        task.run();
                        history.setStatus("SUCCESS");
                    } catch (Exception ex) {
                        logger.error("Job {} failed", job.getJobName(), ex);
                        history.setStatus("FAILED");
                        history.setErrorMessage(ex.getMessage());
                    }

                    jobHistoryRepository.save(history);
                };

                taskRegistrar.addTriggerTask(wrappedTask, trigger);
            }
        }
    }

    private Runnable getRunnableBean(String beanName) {
        try {
            return (Runnable) applicationContext.getBean(beanName);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No such bean found for job: " + beanName);
        }
    }

}
