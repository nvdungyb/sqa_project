package com.java.be_ticket_booking.repository;

import com.java.be_ticket_booking.model.Movie;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepo extends
        PagingAndSortingRepository<Movie, Long>,
        JpaRepository<Movie, Long> {
    boolean existsByTitle(@NotBlank String title);

    List<Movie> findByTitleContaining(String title, Pageable pages);

    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.genre LIKE %:keyword%")
    List<Movie> findByGenresNameContainning(@Param("keyword") String keyword, Pageable pages);
}
