package com.java.be_ticket_booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeTicketBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeTicketBookingApplication.class, args);
    }

}
