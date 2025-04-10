package com.java.be_ticket_booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.be_ticket_booking.model.CinemaShow;

@Repository
public interface ShowRepository extends JpaRepository<CinemaShow, String> {

}
