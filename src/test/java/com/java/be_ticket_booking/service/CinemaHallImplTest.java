package com.java.be_ticket_booking.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.repository.CinemaHallRepository;
import com.java.be_ticket_booking.request.CinemaHallRequest;
import com.java.be_ticket_booking.response.ErrorResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.security.InputValidationFilter;
import com.java.be_ticket_booking.service.impl.CinemaHallImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

public class CinemaHallImplTest {

    @InjectMocks
    private CinemaHallImpl cinemaHallService;

    @Mock
    private CinemaHallRepository hallREPO;

    @Mock
    private CinemaSeatService hallSeatSER;

    @Mock
    private InputValidationFilter inputFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test newHall
    @Test
    public void testNewHall_InvalidName_ReturnsError() {
        CinemaHall hall = new CinemaHall();
        hall.setName("Invalid@Name");
        when(inputFilter.checkInput("Invalid@Name")).thenReturn(false);

        MyApiResponse response = cinemaHallService.newHall(hall);

        assertTrue(response instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response;
        assertEquals("Illeagal charaters in name", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void testNewHall_DuplicateName_ReturnsError() {
        CinemaHall hall = new CinemaHall();
        hall.setName("Hall1");
        when(inputFilter.checkInput("Hall1")).thenReturn(true);
        when(hallREPO.existsByName("Hall1")).thenReturn(true);

        MyApiResponse response = cinemaHallService.newHall(hall);

        assertTrue(response instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response;
        assertEquals("This hall is existed", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void testNewHall_InvalidRowCol_ReturnsError() {
        CinemaHall hall = new CinemaHall();
        hall.setName("Hall1");
        hall.setTotalRow(4);
        hall.setTotalCol(6);
        when(inputFilter.checkInput("Hall1")).thenReturn(true);
        when(hallREPO.existsByName("Hall1")).thenReturn(false);

        MyApiResponse response = cinemaHallService.newHall(hall);

        assertTrue(response instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response;
        assertEquals("Row/Column number must be greater than 5", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void testNewHall_Success() {
        CinemaHall hall = new CinemaHall();
        hall.setName("Hall1");
        hall.setTotalRow(6);
        hall.setTotalCol(6);
        when(inputFilter.checkInput("Hall1")).thenReturn(true);
        when(hallREPO.existsByName("Hall1")).thenReturn(false);
        when(hallREPO.save(any(CinemaHall.class))).thenReturn(hall);
        when(hallREPO.findByName("Hall1")).thenReturn(Optional.of(hall));

        MyApiResponse response = cinemaHallService.newHall(hall);

        assertEquals("Success", response.getMessage());
        verify(hallSeatSER, times(1)).CreateListSeats(hall);
    }

    // Test editHall
    @Test
    public void testEditHall_InvalidName_ReturnsError() {
        CinemaHallRequest request = new CinemaHallRequest("Invalid@Name", 6, 6);
        when(inputFilter.checkInput("Invalid@Name")).thenReturn(false);

        MyApiResponse response = cinemaHallService.editHall("1", request);

        assertTrue(response instanceof ErrorResponse);
        ErrorResponse error = (ErrorResponse) response;
        assertEquals("Illeagal charaters in name", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void testEditHall_HallNotFound_ThrowsException() {
        CinemaHallRequest request = new CinemaHallRequest("Hall1", 6, 6);
        when(inputFilter.checkInput("Hall1")).thenReturn(true);
        when(hallREPO.findById("1")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> cinemaHallService.editHall("1", request));
    }

    @Test
    public void testEditHall_ChangeRowCol_Success() {
        CinemaHall hall = new CinemaHall();
        hall.setId("1");
        hall.setName("OldName");
        hall.setTotalRow(6);
        hall.setTotalCol(6);
        CinemaHallRequest request = new CinemaHallRequest("NewName", 8, 8);
        when(inputFilter.checkInput("NewName")).thenReturn(true);
        when(hallREPO.findById("1")).thenReturn(Optional.of(hall));
        when(hallREPO.save(any(CinemaHall.class))).thenReturn(hall);

        MyApiResponse response = cinemaHallService.editHall("1", request);

        assertEquals("Success", response.getMessage());
        verify(hallSeatSER, times(1)).RemoveAllSeatsFromHall("1");
        verify(hallSeatSER, times(1)).CreateListSeats(hall);
        assertEquals("NewName", hall.getName());
        assertEquals(8, hall.getTotalRow());
        assertEquals(8, hall.getTotalCol());
    }

    @Test
    public void testEditHall_NoChangeRowCol_Success() {
        CinemaHall hall = new CinemaHall();
        hall.setId("1");
        hall.setName("OldName");
        hall.setTotalRow(6);
        hall.setTotalCol(6);
        CinemaHallRequest request = new CinemaHallRequest("NewName", 6, 6);
        when(inputFilter.checkInput("NewName")).thenReturn(true);
        when(hallREPO.findById("1")).thenReturn(Optional.of(hall));
        when(hallREPO.save(any(CinemaHall.class))).thenReturn(hall);

        MyApiResponse response = cinemaHallService.editHall("1", request);

        assertEquals("Success", response.getMessage());
        verify(hallSeatSER, never()).RemoveAllSeatsFromHall(anyString());
        verify(hallSeatSER, never()).CreateListSeats(any(CinemaHall.class));
        assertEquals("NewName", hall.getName());
    }

    // Test removeHall
    @Test
    public void testRemoveHall_HallNotFound_ThrowsException() {
        when(hallREPO.findById("1")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> cinemaHallService.removeHall("1"));
    }

    @Test
    public void testRemoveHall_Success() {
        CinemaHall hall = new CinemaHall();
        hall.setId("1");
        when(hallREPO.findById("1")).thenReturn(Optional.of(hall));

        MyApiResponse response = cinemaHallService.removeHall("1");

        assertEquals("Success", response.getMessage());
        verify(hallSeatSER, times(1)).RemoveAllSeatsFromHall("1");
        verify(hallREPO, times(1)).delete(hall);
    }

    // Test isExistByName
    @Test
    public void testIsExistByName_Exists_ReturnsTrue() {
        when(hallREPO.existsByName("Hall1")).thenReturn(true);

        boolean result = cinemaHallService.isExistByName("Hall1");

        assertTrue(result);
    }

    @Test
    public void testIsExistByName_NotExists_ReturnsFalse() {
        when(hallREPO.existsByName("Hall1")).thenReturn(false);

        boolean result = cinemaHallService.isExistByName("Hall1");

        assertFalse(result);
    }

    // Test isExistById
    @Test
    public void testIsExistById_Exists_ReturnsTrue() {
        when(hallREPO.existsById("1")).thenReturn(true);

        boolean result = cinemaHallService.isExistById("1");

        assertTrue(result);
    }

    @Test
    public void testIsExistById_NotExists_ReturnsFalse() {
        when(hallREPO.existsById("1")).thenReturn(false);

        boolean result = cinemaHallService.isExistById("1");

        assertFalse(result);
    }

    // Test getAllHalls
    @Test
    public void testGetAllHalls_ReturnsList() {
        CinemaHall hall1 = new CinemaHall();
        CinemaHall hall2 = new CinemaHall();
        List<CinemaHall> halls = Arrays.asList(hall1, hall2);
        when(hallREPO.findAll()).thenReturn(halls);

        List<CinemaHall> result = cinemaHallService.getAllHalls();

        assertEquals(2, result.size());
        assertEquals(halls, result);
    }

    // Test getHallById
    @Test
    public void testGetHallById_HallNotFound_ThrowsException() {
        when(hallREPO.findById("1")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> cinemaHallService.getHallById("1"));
    }

    @Test
    public void testGetHallById_Success() {
        CinemaHall hall = new CinemaHall();
        hall.setId("1");
        when(hallREPO.findById("1")).thenReturn(Optional.of(hall));

        CinemaHall result = cinemaHallService.getHallById("1");

        assertEquals(hall, result);
    }
}