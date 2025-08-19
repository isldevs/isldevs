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
package com.base.core.schedule.model;


import jakarta.persistence.*;

import java.sql.Timestamp;

/**
 * @author YISivlay
 */
@Entity
@Table(name = "scheduled_job_history")
public class ScheduledJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "executed_at", nullable = false)
    private Timestamp executedAt;

    @Column(name = "next_executed_at")
    private Timestamp nextExecutedAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    public ScheduledJobHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Timestamp getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Timestamp executedAt) {
        this.executedAt = executedAt;
    }

    public Timestamp getNextExecutedAt() {
        return nextExecutedAt;
    }

    public void setNextExecutedAt(Timestamp nextExecutedAt) {
        this.nextExecutedAt = nextExecutedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
