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
package com.base.config.data;

import java.util.Map;

/**
 * @author YISivlay
 */
public class RequestLog {

  private final long startTime;
  private final long stopTime;
  private final String method;
  private final String url;
  private final Map<String, String[]> parameters;

  public RequestLog(Builder builder) {
    this.startTime = builder.startTime;
    this.stopTime = builder.stopTime;
    this.method = builder.method;
    this.url = builder.url;
    this.parameters = builder.parameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private long startTime;
    private long stopTime;
    private String method;
    private String url;
    private Map<String, String[]> parameters;

    public RequestLog build() {
      return new RequestLog(this);
    }

    public Builder startTime(long startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder stopTime(long stopTime) {
      this.stopTime = stopTime;
      return this;
    }

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder parameters(Map<String, String[]> parameters) {
      this.parameters = parameters;
      return this;
    }
  }

  public long getStartTime() {
    return startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public String getMethod() {
    return method;
  }

  public String getUrl() {
    return url;
  }

  public Map<String, String[]> getParameters() {
    return parameters;
  }
}
