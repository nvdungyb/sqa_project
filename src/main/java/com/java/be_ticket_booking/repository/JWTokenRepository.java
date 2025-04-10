package com.java.be_ticket_booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.be_ticket_booking.model.JWTToken;

public interface JWTokenRepository extends JpaRepository<JWTToken, String> {
	
	Optional<JWTToken> findByUserId(String id);
	Optional<JWTToken> findByRefreshToken(String refresh_token);
}
