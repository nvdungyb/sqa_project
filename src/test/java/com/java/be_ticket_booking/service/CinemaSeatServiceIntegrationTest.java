package com.java.be_ticket_booking.service;

import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.model.CinemaSeat;
import com.java.be_ticket_booking.model.enumModel.ESeat;
import com.java.be_ticket_booking.model.enumModel.ESeatStatus;
import com.java.be_ticket_booking.repository.CinemaHallRepository;
import com.java.be_ticket_booking.repository.CinemaSeatRepository;
import com.java.be_ticket_booking.request.SeatEditRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // để tự động rollback sau mỗi test
public class CinemaSeatServiceIntegrationTest {

    @Autowired
    private CinemaSeatService cinemaSeatService;

    @Autowired
    private CinemaSeatRepository seatRepo;

    @Autowired
    private CinemaHallRepository hallRepo;

    private CinemaHall hall;

    @BeforeEach
    void setUp() {
        hall = new CinemaHall();
        hall.setName("Test Hall");
        hall.setTotalRow(5);
        hall.setTotalCol(5);
        hall = hallRepo.save(hall);
    }

    @Test
    void testCreateListSeats_Integration() {
        cinemaSeatService.CreateListSeats(hall);

        List<CinemaSeat> seats = seatRepo.findByCinemaHallId(hall.getId());
        assertEquals(25, seats.size());
    }

    @Test
    void testRemoveAllSeatsFromHall_Integration() {
        cinemaSeatService.CreateListSeats(hall);
        cinemaSeatService.RemoveAllSeatsFromHall(hall.getId());

        List<CinemaSeat> seats = seatRepo.findByCinemaHallId(hall.getId());
        assertTrue(seats.isEmpty());
    }

    @Test
    void testEditSeat_Success_Integration() {
        cinemaSeatService.CreateListSeats(hall);

        SeatEditRequest request = new SeatEditRequest(0, 0, "PREMIUM", "UNAVAILABLE");
        MyApiResponse response = cinemaSeatService.Edit(hall.getId(), request);

        assertEquals("Success", response.getMessage());

        Optional<CinemaSeat> seatOpt = seatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 0, 0);
        assertTrue(seatOpt.isPresent());
        assertEquals(ESeat.PREMIUM, seatOpt.get().getSeatType());
        assertEquals(ESeatStatus.UNAVAILABLE, seatOpt.get().getStatus());
    }
}
