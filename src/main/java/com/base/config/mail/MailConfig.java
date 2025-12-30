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
package com.base.config.mail;


import com.base.config.GlobalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * @author YISivlay
 */
@Configuration
public class MailConfig {

    private final GlobalConfig config;

    @Autowired
    public MailConfig(GlobalConfig config) {
        this.config = config;
    }

    @Bean
    public JavaMailSender javaMailSender() {

        String host = config.getConfigValue("EMAIL_HOST", "smtp.gmail.com");
        String port = config.getConfigValue("EMAIL_PORT", "587");
        String username = config.getConfigValue("EMAIL_USERNAME", "sender@gmail.com");
        String password = config.getConfigValue("EMAIL_APP_PASSWORD", "16_char_app_password");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(Integer.parseInt(port));
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.mime.charset", "UTF-8");
        mailSender.setDefaultEncoding("UTF-8");

        return mailSender;
    }

}
