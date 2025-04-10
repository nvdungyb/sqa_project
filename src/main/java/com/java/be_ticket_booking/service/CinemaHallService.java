package com.java.be_ticket_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.request.CinemaHallRequest;
import com.java.be_ticket_booking.response.MyApiResponse;

@Service
public interface CinemaHallService {
	List<CinemaHall> getAllHalls();
	CinemaHall getHallById(String id);
	
	MyApiResponse newHall(CinemaHall c);
	MyApiResponse editHall(String hallID, CinemaHallRequest c);
	MyApiResponse removeHall(String HallID);
	
	boolean isExistByName(String hallName);
	boolean isExistById(String ID);
}
