package com.java.be_ticket_booking.service;


public interface EmailService {
	void sendMail(String toMail, String subject, String body);
}
