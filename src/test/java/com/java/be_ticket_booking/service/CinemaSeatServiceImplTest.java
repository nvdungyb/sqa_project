package com.java.be_ticket_booking.service;

import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.model.CinemaSeat;
import com.java.be_ticket_booking.model.enumModel.ESeat;
import com.java.be_ticket_booking.model.enumModel.ESeatStatus;
import com.java.be_ticket_booking.repository.CinemaSeatRepository;
import com.java.be_ticket_booking.request.SeatEditRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.SeatsResponse;
import com.java.be_ticket_booking.service.impl.CinemaSeatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CinemaSeatServiceImplTest {

    @Mock
    private CinemaSeatRepository hallSeatRepo;

    @InjectMocks
    private CinemaSeatServiceImpl seatService;

    private CinemaHall hall;
    private CinemaSeat seat;
    @Autowired
    private CinemaSeatService cinemaSeatService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        hall = new CinemaHall();
        hall.setId("1");
        hall.setTotalRow(5);
        hall.setTotalCol(5);

        seat = new CinemaSeat(hall, 0, 0, ESeat.REGULAR);
    }

    @Test
    public void testCreateListSeats() {
        seatService.CreateListSeats(hall);
        verify(hallSeatRepo, times(25)).save(any(CinemaSeat.class));
    }

    @Test
    public void testIsExist_True() {
        when(hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("1", 0, 0))
                .thenReturn(Optional.of(seat));

        assertTrue(seatService.isExist("1", 0, 0));
    }

    @Test
    public void testIsExist_False() {
        when(hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("1", 1, 1))
                .thenReturn(Optional.empty());

        assertFalse(seatService.isExist("1", 1, 1));
    }

    @Test
    public void testRemoveAllSeatsFromHall() {
        seatService.RemoveAllSeatsFromHall("1");
        verify(hallSeatRepo, times(1)).deleteAllByCinemaHallId("1");
    }

    @Test
    public void testGetAllSeatsFromHall() {
        when(hallSeatRepo.findByCinemaHallId("1"))
                .thenReturn(Arrays.asList(seat));

        List<SeatsResponse> responses = seatService.getAllSeatsFromHall("1");
        assertEquals(1, responses.size());
        assertNotNull(responses.get(0).getPrice());
        assertNotNull(responses.get(0).getName());
    }

    // todo: test sai do SeatEdit constructor truyền vào type là kiểu String mà phương thức Edit cần kiểu ESeat
    @Test
    public void testEdit_Success() {
        SeatEditRequest req = new SeatEditRequest(0, 0, "REGULAR", ESeat.PREMIUM.name());

        when(hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("1", 0, 0))
                .thenReturn(Optional.of(seat));

        MyApiResponse response = seatService.Edit("1", req);

        assertEquals("Success", response.getMessage());
        verify(hallSeatRepo, times(1)).save(seat);
    }

    @Test
    public void testEdit_SeatNotFound() {
        SeatEditRequest req = new SeatEditRequest(0, 0, "REGULAR", "AVAILABLE");

        when(hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("1", 0, 0))
                .thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> seatService.Edit("1", req));
    }

    // todo: sai kiểu tham số như trên
    @Test
    public void testEdit_InvalidType() {
        SeatEditRequest req = new SeatEditRequest(0, 0, "INVALID", "AVAILABLE");

        when(hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("1", 0, 0))
                .thenReturn(Optional.of(seat));

        MyApiResponse response = seatService.Edit("1", req);
        assertEquals("Type is not found. It must be REGULAR or PREMIUM", response.getMessage());
    }

    @Test
    public void testEdit_InvalidStatus() {
        SeatEditRequest req = new SeatEditRequest(0, 0, "REGULAR", "INVALID");

        when(hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("1", 0, 0))
                .thenReturn(Optional.of(seat));

        MyApiResponse response = seatService.Edit("1", req);
        assertEquals("Status is not found. It must be AVAILABLE or UNAVAILABLE", response.getMessage());
    }

    @Test
    public void testGetSeatType_regular() {
        ESeat result = invokeGetSeatType("REGULAR");
        assertEquals(ESeat.REGULAR, result);
    }

    @Test
    public void testGetSeatType_premium() {
        ESeat result = invokeGetSeatType("PREMIUM");
        assertEquals(ESeat.PREMIUM, result);
    }

    @Test
    public void testGetSeatType_invalid() {
        ESeat result = invokeGetSeatType("VIP");
        assertNull(result);
    }

    @Test
    public void testGetSeatType_nullInput() {
        assertThrows(RuntimeException.class, () -> {
            invokeGetSeatType(null);
        });
    }

    @Test
    public void testGetSeatStatus_available() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ESeatStatus result = invokeGetSeatStatus("AVAILABLE");
        assertEquals(ESeatStatus.AVAILABLE, result);
    }

    @Test
    public void testGetSeatStatus_unavailable() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ESeatStatus result = invokeGetSeatStatus("UNAVAILABLE");
        assertEquals(ESeatStatus.UNAVAILABLE, result);
    }

    @Test
    public void testGetSeatStatus_invalid() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ESeatStatus result = invokeGetSeatStatus("BROKEN");
        assertNull(result);
    }

    @Test
    public void testGetSeatStatus_nullInput() {
        assertThrows(RuntimeException.class, () -> {
            invokeGetSeatStatus(null);
        });
    }

    // Access private methods using reflection
    private ESeat invokeGetSeatType(String type) {
        try {
            var method = CinemaSeatServiceImpl.class.getDeclaredMethod("getSeatType", String.class);
            method.setAccessible(true);
            return (ESeat) method.invoke(cinemaSeatService, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ESeatStatus invokeGetSeatStatus(String status) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        var method = CinemaSeatServiceImpl.class.getDeclaredMethod("getSeatStatus", String.class);
        method.setAccessible(true);
        return (ESeatStatus) method.invoke(cinemaSeatService, status);
    }
}
