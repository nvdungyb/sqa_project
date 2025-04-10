package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.service.EmailService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@Setter
@Getter
public class EmailServiceImpl implements EmailService {

    @Value("${app.default_sender}")
    private String default_sender;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendMail(String toMail, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(default_sender);
        mail.setTo(toMail);
        mail.setSubject(subject);
        mail.setText(body);

        mailSender.send(mail);
    }

    public void setDefault_sender(String default_sender) {
        this.default_sender = default_sender;
    }

    public String getDefault_sender() {
        return default_sender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
}
