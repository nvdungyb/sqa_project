package com.java.be_ticket_booking.service;

import org.springframework.stereotype.Service;

import com.java.be_ticket_booking.request.LoginRequest;
import com.java.be_ticket_booking.request.SignUpRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public interface AuthenticationService {
	public MyApiResponse signup(SignUpRequest request, String ip);
	public AuthenticationResponse login(LoginRequest request, HttpServletRequest servletRequest, boolean adminLogin);
	public AuthenticationResponse refreshAccessToken(String refreshToken, HttpServletRequest servletRequest);
	public void veriyCode(String code, HttpServletResponse response);
}