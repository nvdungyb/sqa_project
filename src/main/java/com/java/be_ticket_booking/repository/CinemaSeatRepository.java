package com.java.be_ticket_booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.model.CinemaSeat;

@Repository
public interface CinemaSeatRepository extends JpaRepository<CinemaSeat, Long> {
	List<CinemaSeat> findByCinemaHall(CinemaHall hall);
	List<CinemaSeat> findByCinemaHallId(String hallID);
	
	Optional<CinemaSeat> findByCinemaHallIdAndRowIndexAndColIndex(String hallID, int row, int col);
	void deleteAllByCinemaHallId(String hallID);
}
