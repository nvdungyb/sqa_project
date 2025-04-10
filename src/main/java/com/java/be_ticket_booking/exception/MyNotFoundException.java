package com.java.be_ticket_booking.exception;

import com.java.be_ticket_booking.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class MyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8914974118258357770L;
	
	private ErrorResponse error;
	private String message;
	
	public MyNotFoundException(ErrorResponse error) {
		this.error = error;
	}
	
	public MyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public MyNotFoundException(String message) {
		super(message);
		this.message = message;
	}
	
	public ErrorResponse getErrorResponse() {
		return this.error;
	}
	
	public void setErrorResponse(ErrorResponse error) {
		this.error = error;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
