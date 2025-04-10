package com.java.be_ticket_booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.be_ticket_booking.model.ShowSeat;
import com.java.be_ticket_booking.model.enumModel.ESeatStatus;
import jakarta.transaction.Transactional;

@Transactional
@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, String> {
	int countByShowIdAndStatus(String show_id, ESeatStatus status);
	List<ShowSeat> findByShowId(String showId);
	void deleteAllByShowId(String show_id);
	Optional<ShowSeat> findByIdAndShowId(String id, String showId);
}
