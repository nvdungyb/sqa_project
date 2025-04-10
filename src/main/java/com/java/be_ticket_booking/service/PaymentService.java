package com.java.be_ticket_booking.service;

import java.util.List;

import com.java.be_ticket_booking.model.Payment;
import com.java.be_ticket_booking.request.HashRequest;
import com.java.be_ticket_booking.request.PaymentRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.PaymentResponse;

public interface PaymentService {
	
	public PaymentResponse create(String username, PaymentRequest request, String ip_addr);
	public PaymentResponse getFromId(String username, String payment_id);
	public List<PaymentResponse> getAllPaymentsOfUser(String username);
	public boolean checkPaymentInfo(PaymentRequest request);
	public MyApiResponse verifyPayment(String username, String payment_id);
	
	public String createHash(HashRequest rawdata);
	public void addPaymentMail(Payment payment);

}
