package com.java.be_ticket_booking.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    private EmailServiceImpl emailService;
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailServiceImpl();
        emailService.setMailSender(mailSender);
        emailService.setDefault_sender("default@sender.com");
    }

    @Test
    void sendMail() {
        // Arrange
        String to = "test@example.com";
        String subject = "Hello";
        String body = "Test mail body";

        // Act
        emailService.sendMail(to, subject, body);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMail = captor.getValue();
        assertEquals("default@sender.com", sentMail.getFrom());
        assertArrayEquals(new String[]{to}, sentMail.getTo());
        assertEquals(subject, sentMail.getSubject());
        assertEquals(body, sentMail.getText());
    }

    @Test
    void setDefault_sender() {
        emailService.setDefault_sender("new_sender@example.com");
        assertEquals("new_sender@example.com", emailService.getDefault_sender());
    }

    @Test
    void getDefault_sender() {
        // default_sender đã được set trong setUp()
        assertEquals("default@sender.com", emailService.getDefault_sender());
    }

    @Test
    void setMailSender() {
        JavaMailSender newMailSender = mock(JavaMailSender.class);
        emailService.setMailSender(newMailSender);
        assertSame(newMailSender, emailService.getMailSender());
    }

    @Test
    void getMailSender() {
        assertSame(mailSender, emailService.getMailSender());
    }
}
