package com.java.be_ticket_booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "SpamUser")
public class SpamUser {

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false, length = 36, insertable = false)
    private String id;

    @OneToOne
    @NotNull
    private Account user;

    @NotNull
    @Column(name = "spamTimes")
    private int spamTimes;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = true, updatable = true)
    private Date update_at;

    public SpamUser() {
    }

    public SpamUser(Account user) {
        this.user = user;
        this.spamTimes = 1;
    }

    public String getId() {
        return id;
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

    public int getSpamTimes() {
        return this.spamTimes;
    }

    public void setSpamTimes(int times) {
        this.spamTimes = times;
    }

    public int increase() {
        this.spamTimes += 1;
        return this.spamTimes;
    }

    public void setTimes(int times) {
        this.spamTimes = times;
    }

    @PrePersist
    public void assignUUID() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}




