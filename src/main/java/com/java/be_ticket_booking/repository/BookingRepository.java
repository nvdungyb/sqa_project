package com.java.be_ticket_booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.be_ticket_booking.model.Booking;
import com.java.be_ticket_booking.model.enumModel.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, String> {
	List<Booking> findAllByStatus(BookingStatus status);
	List<Booking> findAllByUserId(String user_id);
	
	Optional<Booking> findByIdAndUserId(String booking_id, String user_id);
	
	@Query("SELECT b FROM Booking b WHERE b.user.id = :user_id AND b.show.movie.id = :movie_id AND b.status = :status")
	Optional<Booking> findByUserIdAndMovieIdAndStatus(@Param("user_id") String user_id, @Param("movie_id") long movie_id, @Param("status") BookingStatus status);
	
	int countByShowId(String show_id);
}
