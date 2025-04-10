package com.java.be_ticket_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.java.be_ticket_booking.request.ShowRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.ShowInfoResponse;
import com.java.be_ticket_booking.response.ShowSeatResponse;

@Service
public interface CinemaShowService {
	public MyApiResponse addShow(ShowRequest show);
	public List<MyApiResponse> addListShows(List<ShowRequest> shows);
	public MyApiResponse deleteShow(String show_id);
	public MyApiResponse deleteShowByHallIDMovieID(ShowRequest showReq);
	public MyApiResponse updateShow(String show_id, ShowRequest showReq);
	
	public ShowInfoResponse getShowInfo(String show_id);
	public List<ShowInfoResponse> getAllShows();
	public List<ShowInfoResponse> getAllShowByHallID(String hallID);
	public List<ShowInfoResponse> getAllShowByMovieID(String movieID);
	public List<ShowSeatResponse> getAllShowSeats(String showID); 
}
