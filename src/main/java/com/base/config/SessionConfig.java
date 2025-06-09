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


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author YISivlay
 */
@Configuration
@EnableJdbcHttpSession
public class SessionConfig implements BeanClassLoaderAware {

    private static final String CREATE_SESSION_ATTRIBUTE_QUERY = """
        INSERT INTO SPRING_SESSION_ATTRIBUTES (SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES)
        VALUES (?, ?, ?::jsonb)
        """;

    private static final String UPDATE_SESSION_ATTRIBUTE_QUERY = """
        UPDATE SPRING_SESSION_ATTRIBUTES
        SET ATTRIBUTE_BYTES = ?::jsonb
        WHERE SESSION_PRIMARY_ID = ?
        AND ATTRIBUTE_NAME = ?
        """;

    private ClassLoader classLoader;

    @Bean
    SessionRepositoryCustomizer<JdbcIndexedSessionRepository> customizer() {
        return (sessionRepository) -> {
            sessionRepository.setCreateSessionAttributeQuery(CREATE_SESSION_ATTRIBUTE_QUERY);
            sessionRepository.setUpdateSessionAttributeQuery(UPDATE_SESSION_ATTRIBUTE_QUERY);
        };
    }

    @Bean("springSessionConversionService")
    public GenericConversionService springSessionConversionService(ObjectMapper objectMapper) {
        ObjectMapper copy = objectMapper.copy();
        copy.registerModules(SecurityJackson2Modules.getModules(this.classLoader));
        GenericConversionService converter = new GenericConversionService();
        converter.addConverter(Object.class, byte[].class, new SerializingConverter(new JsonSerializer(copy)));
        converter.addConverter(byte[].class, Object.class, new DeserializingConverter(new JsonDeserializer(copy)));
        return converter;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    static class JsonSerializer implements Serializer<Object> {
        private final ObjectMapper objectMapper;

        JsonSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void serialize(Object object, OutputStream outputStream) throws IOException {
            String json = this.objectMapper.writeValueAsString(object);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    static class JsonDeserializer implements Deserializer<Object> {
        private final ObjectMapper objectMapper;

        JsonDeserializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Object deserialize(InputStream inputStream) throws IOException {
            return this.objectMapper.readValue(inputStream, Object.class);
        }
    }
}

