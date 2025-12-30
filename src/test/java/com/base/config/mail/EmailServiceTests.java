package com.base.config.mail;

import com.base.config.GlobalConfig;
import com.base.core.exception.ErrorException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceTests {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private GlobalConfig config;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage((Session) null));
    }

    @Test
    void send_shouldSendEmailSuccessfully() {
        when(config
                .getConfigValue("EMAIL_SENDER_NAME", "Sender Name"))
                .thenReturn("iSLDevs");

        when(config.getConfigValue("EMAIL_USERNAME", "sender@gmail.com"))
                .thenReturn("isldevs168@gmail.com");

        emailService.send(
                "yisivlay95@gmail.com",
                "Test Subject",
                "Plain text body",
                "<p>HTML body</p>"
        );

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

}
