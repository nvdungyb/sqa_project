package com.java.be_ticket_booking.service;

import com.java.be_ticket_booking.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EmailServiceImplTest {

    private EmailServiceImpl emailService;
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailServiceImpl();
        emailService.setMailSender(mailSender); // setter bạn phải có
        emailService.setDefault_sender("test@domain.com"); // gán trực tiếp, không cần spy hay doReturn
    }

    @Test
    void testSendMail_success() {
        String to = "recipient@domain.com";
        String subject = "Test Subject";
        String body = "Hello world";

        emailService.sendMail(to, subject, body);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("test@domain.com", sentMessage.getFrom());
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }

    @Test
    void testSendMail_withEmptySubjectAndBody() {
        String to = "recipient@domain.com";

        emailService.sendMail(to, "", "");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("test@domain.com", message.getFrom());
        assertEquals(to, message.getTo()[0]);
        assertEquals("", message.getSubject());
        assertEquals("", message.getText());
    }

    @Test
    void testSendMail_withNullValues() {
        assertThrows(IllegalArgumentException.class, () ->
                emailService.sendMail(null, null, null)
        );
    }

    @Test
    void testSendMail_throwsException() {
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            emailService.sendMail("to@domain.com", "subj", "body");
        });

        assertEquals("Mail server error", ex.getMessage());
    }
}
