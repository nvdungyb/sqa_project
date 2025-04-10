package com.java.be_ticket_booking.service;

import org.springframework.stereotype.Service;

import com.java.be_ticket_booking.model.Account;
import com.java.be_ticket_booking.model.JWTToken;
import com.java.be_ticket_booking.response.AuthenJWTokenResponse;

@Service
public interface JWTokenService {
	
	public AuthenJWTokenResponse getFromRefreshToken(String refresh_token);
	public AuthenJWTokenResponse getData(Account user);
	public JWTToken saveInfo(Account user, String accessToken, String refreshToken);
	public JWTToken updateInfo(JWTToken data, String accessToken, String refreshToken);
	public String getAccessToken(Account user);
	public String getRefreshToken(Account user);
	public String setAccessToken(JWTToken data, String accessToken);
	public String setRefreshToken(JWTToken data, String refreshToken);
}
