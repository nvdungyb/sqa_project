package com.java.be_ticket_booking.model;

import com.java.be_ticket_booking.model.enumModel.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Booking")
public class Booking {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false, length = 36, insertable = false)
    private String id;

    @ManyToOne
    @NotNull
    private Account user;

    @ManyToOne
    @NotNull
    private CinemaShow show;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private Date create_at;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = true, updatable = true)
    private Date update_at;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<ShowSeat> seats;

    public Booking() {
    }

    public Booking(Booking booking) {
        this.user = booking.getUser();
        this.show = booking.getShow();
        this.seats = booking.getSeats();
        this.status = BookingStatus.PENDING;
    }

    public Booking(Account user, CinemaShow show, List<ShowSeat> seats) {
        this.user = user;
        this.show = show;
        this.seats = seats;
        this.status = BookingStatus.PENDING;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getUser() {
        return this.user;
    }

    public void setUser(Account user) {
        this.user = user;
    }

    public CinemaShow getShow() {
        return this.show;
    }

    public void setShow(CinemaShow show) {
        this.show = show;
    }

    public BookingStatus getStatus() {
        return this.status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Date getCreateAt() {
        return this.create_at;
    }

    public Date getUpdateAt() {
        return this.update_at;
    }

    public List<ShowSeat> getSeats() {
        return this.seats;
    }

    public void setSeats(List<ShowSeat> seats) {
        this.seats = seats;
    }

    public void addSeat(ShowSeat seat) {
        this.seats.add(seat);
    }

    public void removeSeat(ShowSeat seat) {
        this.seats.remove(seat);
    }

    public boolean isEmptySeats() {
        return this.seats.isEmpty();
    }

    public List<String> getNameOfSeats() {
        List<String> names = new ArrayList<>();
        for (ShowSeat seat : this.seats)
            names.add(seat.getCinemaSeat().getName());
        return names;
    }

    public double getPriceFromListSeats() {
        double res = 0;
        for (ShowSeat seat : this.seats)
            res += seat.getCinemaSeat().getPrice();
        return res;
    }

    public void setCreateAt(Date from) {
        this.create_at = from;
    }

    @PrePersist
    public void assignUUID() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}








