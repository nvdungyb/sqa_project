package com.java.be_ticket_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.request.SeatEditRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.SeatsResponse;

@Service
public interface CinemaSeatService {
	
	List<SeatsResponse> getAllSeatsFromHall(String hallID);
	
	public void CreateListSeats(CinemaHall hall);
	public void RemoveAllSeatsFromHall(String hallID);
	
	public MyApiResponse Edit(String hallID, SeatEditRequest seatReq);
	
	public boolean isExist(String hallID, int row, int column);
}
