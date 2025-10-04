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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author YISivlay
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    private final GlobalConfig globalConfig;

    private final ObjectMapper objectMapper;

    @Autowired
    public WebConfig(final GlobalConfig globalConfig,
                     final ObjectMapper objectMapper) {
        this.globalConfig = globalConfig;
        this.objectMapper = objectMapper;
    }

    /** CORS configuration for all endpoints */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(getAllowedOrigins())
                .allowedMethods("GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "PATCH",
                                "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization",
                                "Content-Disposition",
                                "X-CSRF-TOKEN")
                .allowCredentials(true)
                .maxAge(3600); // cache preflight
                                                                                                                                                                                                                                                                                 // response for 1h
    }

    private String[] getAllowedOrigins() {
        // Get origins from configuration or use defaults
        String originsConfig = globalConfig.getConfigValue("CORS_ALLOWED_ORIGINS");
        if (originsConfig != null && !originsConfig.trim()
                                                   .isEmpty()) {
            return originsConfig.split(",");
        }
        return new String[]{"https://127.0.0.1:8443", "http://127.0.0.1:8080", "http://127.0.0.1:3000", "http://localhost:3000", "http://localhost:8080"};
    }

    /** Use PathPatternParser for better performance and pattern matching */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPatternParser(new PathPatternParser());
    }

    /** Async support configuration */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30000); // 30 seconds

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(50);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("async-exec-");
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();

        configurer.setTaskExecutor(taskExecutor);
    }

    /** Content negotiation configuration */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(true)
                  .parameterName("format")
                  .ignoreAcceptHeader(false)
                  .useRegisteredExtensionsOnly(false)
                  .defaultContentType(MediaType.APPLICATION_JSON)
                  .mediaType("json",
                             MediaType.APPLICATION_JSON)
                  .mediaType("xml",
                             MediaType.APPLICATION_XML)
                  .mediaType("html",
                             MediaType.TEXT_HTML)
                  .mediaType("text",
                             MediaType.TEXT_PLAIN);
    }

    /** Default servlet handling */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable("default");
    }

    /** Global interceptors */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Logging interceptor
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) {
                String requestId = UUID.randomUUID()
                                       .toString();
                request.setAttribute("requestId",
                                     requestId);
                MDC.put("requestId",
                        requestId);
                MDC.put("clientIP",
                        request.getRemoteAddr());
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
                MDC.clear();
            }
        })
                .addPathPatterns("/**");

        // Performance monitoring interceptor
        registry.addInterceptor(new HandlerInterceptor() {
            private final ThreadLocal<Long> startTime = new ThreadLocal<>();

            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) {
                startTime.set(System.currentTimeMillis());
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
                Long start = startTime.get();
                if (start != null) {
                    long duration = System.currentTimeMillis() - start;
                    if (duration > 1000) { // Log slow requests
                        logger.warn("Slow request: {} {} took {}ms",
                                    request.getMethod(),
                                    request.getRequestURI(),
                                    duration);
                    }
                    startTime.remove();
                }
            }
        })
                .addPathPatterns("/api/v1/**");

        // Security headers interceptor
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object handler,
                                   ModelAndView modelAndView) {
                response.setHeader("X-Content-Type-Options",
                                   "nosniff");
                response.setHeader("X-Frame-Options",
                                   "DENY");
                response.setHeader("X-XSS-Protection",
                                   "1; mode=block");
                response.setHeader("Strict-Transport-Security",
                                   "max-age=31536000; includeSubDomains");
            }
        })
                .addPathPatterns("/**");
    }

    /** Static resource handlers */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/opt/app/uploads/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    /** View controllers for simple mappings */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login")
                .setViewName("login");
        registry.addViewController("/")
                .setViewName("index");
        registry.addViewController("/home")
                .setViewName("home");
        registry.addViewController("/dashboard")
                .setViewName("dashboard");
        registry.addViewController("/error")
                .setViewName("error");
        registry.addViewController("/access-denied")
                .setViewName("access-denied");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    /** Custom message converters */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Jackson JSON converter
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));

        // String converter
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(stringConverter);

        // Byte array converter
        converters.add(new ByteArrayHttpMessageConverter());

        // Resource converter
        converters.add(new ResourceHttpMessageConverter());
    }

    /** Extend existing message converters */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                  .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                  .map(converter -> (MappingJackson2HttpMessageConverter) converter)
                  .forEach(converter -> {
                      ObjectMapper objectMapper = converter.getObjectMapper();
                      objectMapper.registerModule(new JavaTimeModule());
                      objectMapper.registerModule(new Jdk8Module());
                      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                      objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                      objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
                  });
    }

    /** Custom error code resolution */
    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
        resolver.setMessageCodeFormatter(DefaultMessageCodesResolver.Format.POSTFIX_ERROR_CODE);
        return resolver;
    }

}
