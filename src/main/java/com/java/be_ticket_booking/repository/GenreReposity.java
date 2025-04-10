package com.java.be_ticket_booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.be_ticket_booking.model.Genre;
import jakarta.validation.constraints.NotBlank;

public interface GenreReposity extends JpaRepository<Genre, Long>{
	
	boolean existsByGenre(@NotBlank String genre);
	List<Genre> findAllByGenre(String genre);
	List<Genre> findByGenreContaining(String genre);
	Genre findByGenre(String genre);
}
