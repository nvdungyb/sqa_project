package com.java.be_ticket_booking.model;

import jakarta.persistence.*;
import com.java.be_ticket_booking.model.enumModel.PaymentStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Payment")
public class Payment {
	
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false, length = 36, insertable = false)
    private String id;
	
	@OneToOne
    private Booking booking;
	
	@Column(name = "amount")
	private double amount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
    private PaymentStatus status;
	
	@CreationTimestamp
	@Column(name = "create_at", nullable = false, updatable = false)
	private Date create_at;
	
	@UpdateTimestamp
	@Column(name = "update_at", nullable = true, updatable = true)
	private Date update_at;
	
	public Payment() {}
	
	public Payment(Booking booking, double amount) {
		this.booking = booking;
		this.amount = amount;
		this.status = PaymentStatus.PENDING;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Booking getBooking() {
		return this.booking;
	}
	
	public void setBooking(Booking b) {
		this.booking = b;
	}
	
	public double getAmount() {
		return this.amount;
	}
	
	public void setSmount(double amount) {
		this.amount = amount;
	}
	
	public PaymentStatus getStatus() {
		return this.status;
	}
	
	public void setStatus(PaymentStatus status) {
		this.status = status;
	}
	
	public Date getCreateAt() {
    	return this.create_at;
    }
    
    public Date getUpdateAt() {
    	return this.update_at;
    }
	
	
	public void canclePayment() {
		this.status = PaymentStatus.CANCLED;
	}
	
	public void returnPayment() {
		this.status = PaymentStatus.RETURNED;
	}

	@PrePersist
	public void assignUUID() {
		if (this.id == null) {
			this.id = UUID.randomUUID().toString();
		}
	}
}