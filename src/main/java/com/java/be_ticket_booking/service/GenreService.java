package com.java.be_ticket_booking.service;

import java.util.List;

import com.java.be_ticket_booking.model.Genre;
import com.java.be_ticket_booking.response.MyApiResponse;

public interface GenreService {
	
	List<Genre> getGenres();
	
	Genre saveGenre (Genre genre);
	
	MyApiResponse saveListGenres(List<Genre> genre);
	
	Genre getGenre (Long id);

	MyApiResponse deleteGenre (Long id);
	
	Genre updateGenre (Genre genre);

}

