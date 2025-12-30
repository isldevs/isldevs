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
import com.base.core.exception.ErrorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * @author YISivlay
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final GlobalConfig config;

    @Autowired
    public EmailService(JavaMailSender mailSender,
                        GlobalConfig config) {
        this.mailSender = mailSender;
        this.config = config;
    }

    public void send(String sendTo,
                     String subject,
                     String textBody,
                     String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String senderName = config.getConfigValue("EMAIL_SENDER_NAME", "Sender Name");
            String senderEmail = config.getConfigValue("EMAIL_USERNAME", "sender@gmail.com");

            helper.setTo(sendTo);
            helper.setFrom(senderEmail, senderName);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ErrorException("msg.mail.fail", e);
        }
    }

}
