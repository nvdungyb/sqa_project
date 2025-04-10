package com.java.be_ticket_booking.response;

import com.java.be_ticket_booking.model.CinemaSeat;
import com.java.be_ticket_booking.model.enumModel.ESeatStatus;

public class SeatsResponse {
	
	private String name;
	private String type;
	private ESeatStatus status;
	private double price;
	
	public SeatsResponse(CinemaSeat s) {
		this.name = s.getName();
		this.type = s.getSeatType();
		this.status = s.getStatus();
		this.price = s.getPrice();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getStatus() {
		return this.status.name();
	}
	
	public double getPrice() {
		return this.price;
	}
}
