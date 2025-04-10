package com.java.be_ticket_booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.be_ticket_booking.model.SpamUser;

public interface SpamUserRepository extends JpaRepository<SpamUser, String> {
	Optional<SpamUser> findByUserId(String user_id);
	Boolean existsByUserId(String user_id);
}
