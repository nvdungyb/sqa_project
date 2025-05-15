package com.java.be_ticket_booking.service;

import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.CinemaHall;
import com.java.be_ticket_booking.repository.CinemaHallRepository;
import com.java.be_ticket_booking.request.CinemaHallRequest;
import com.java.be_ticket_booking.response.ErrorResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.security.InputValidationFilter;
import com.java.be_ticket_booking.service.impl.CinemaHallImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@Transactional
public class CinemaHallImplTest {

    @InjectMocks
    private CinemaHallImpl cinemaHallService;

    @Mock
    private CinemaHallRepository hallRepo;

    @Mock
    private CinemaSeatService hallSeatService;

    @Mock
    private InputValidationFilter inputFilter;

    private CinemaHall validHall;
    private CinemaHall existingHall;
    private CinemaHallRequest request;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private CinemaHallImpl cinemaHallImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validHall = new CinemaHall();
        validHall.setId("1");
        validHall.setName("Hall A");
        validHall.setTotalCol(10);
        validHall.setTotalRow(10);

        existingHall = new CinemaHall();
        existingHall.setId("hall-id-001");
        existingHall.setName("Hall A");
        existingHall.setTotalCol(10);
        existingHall.setTotalRow(12);

        request = new CinemaHallRequest("Updated Hall", 12, 10);
    }

    // Thiết kế ErrorResponse kế thừa MyApiResponse nhưng không truyền HttpStatus cho supper => status == null
    // MyApiResponse lại để HttpStatus là String => sai
    @Test
    void testNewHallIllegalCharactersInName() {
        when(inputFilter.checkInput(validHall.getName())).thenReturn(false);

        ErrorResponse response = (ErrorResponse) cinemaHallService.newHall(validHall);
        log.info(response.toString());
        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Illeagal charaters in name", response.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }

    // tương tự trả mã lỗi sai
    @Test
    void testNewHall_hallAlreadyExists() {
        when(inputFilter.checkInput(validHall.getName())).thenReturn(true);
        when(hallRepo.existsByName(validHall.getName())).thenReturn(true);

        MyApiResponse response = cinemaHallService.newHall(validHall);

        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("This hall is existed", response.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }

    // Mã lỗi trả sai
    @Test
    void testNewHall_invalidRowOrColumn() {
        validHall.setTotalRow(0); // invalid row
        when(inputFilter.checkInput(validHall.getName())).thenReturn(true);
        when(hallRepo.existsByName(validHall.getName())).thenReturn(false);

        MyApiResponse response = cinemaHallService.newHall(validHall);

        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ((ErrorResponse) response).getStatus());
    }

    @Test
    void testNewHall_success() {
        when(inputFilter.checkInput(validHall.getName())).thenReturn(true);
        when(hallRepo.existsByName(validHall.getName())).thenReturn(false);
        when(hallRepo.findByName(validHall.getName())).thenReturn(Optional.of(validHall));

        MyApiResponse response = cinemaHallService.newHall(validHall);

        verify(hallRepo).save(validHall);
        verify(hallSeatService).CreateListSeats(validHall);

        assertNotNull(response);
        assertEquals("Success", response.getMessage());
    }

    // Test editHall
    @Test
    void testEditHall_illegalCharactersInName() {
        when(inputFilter.checkInput(request.getName())).thenReturn(false);

        MyApiResponse response = cinemaHallService.editHall("hall-id-001", request);

        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Illeagal charaters in name", response.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }

    @Test
    void testEditHall_hallNotFound() {
        when(inputFilter.checkInput(request.getName())).thenReturn(true);
        when(hallRepo.findById("invalid-id")).thenReturn(Optional.empty());

        MyNotFoundException ex = assertThrows(MyNotFoundException.class,
                () -> cinemaHallService.editHall("invalid-id", request));

        assertEquals("Hall is not found", ex.getMessage());
    }

    @Test
    void testEditHall_noSeatStructureChange() {
        when(inputFilter.checkInput(request.getName())).thenReturn(true);
        when(hallRepo.findById(existingHall.getId())).thenReturn(Optional.of(existingHall));

        MyApiResponse response = cinemaHallService.editHall(existingHall.getId(), request);

        verify(hallSeatService, never()).RemoveAllSeatsFromHall(anyString());
        verify(hallSeatService, never()).CreateListSeats(any());
        verify(hallRepo).save(existingHall);

        assertEquals("Success", response.getMessage());
    }

    @Test
    void testEditHall_withSeatStructureChange() {
        CinemaHallRequest hallRequest = new CinemaHallRequest("Updated Hall", 12, 12);

        when(inputFilter.checkInput(hallRequest.getName())).thenReturn(true);
        when(hallRepo.findById(existingHall.getId())).thenReturn(Optional.of(existingHall));

        MyApiResponse response = cinemaHallService.editHall(existingHall.getId(), hallRequest);

        verify(hallSeatService).RemoveAllSeatsFromHall(existingHall.getId());
        verify(hallSeatService).CreateListSeats(existingHall);
        verify(hallRepo).save(existingHall);

        assertEquals("Success", response.getMessage());
    }

    @Test
    void testEditHall_withSeatStructureChangeWithInvalidNumberRowOrCol() {
        CinemaHallRequest hallRequest = new CinemaHallRequest("Updated Hall", 2, 12);

        when(inputFilter.checkInput(hallRequest.getName())).thenReturn(true);
        when(hallRepo.findById(existingHall.getId())).thenReturn(Optional.of(existingHall));

        MyApiResponse response = cinemaHallService.editHall(existingHall.getId(), hallRequest);
        assertInstanceOf(ErrorResponse.class, response);
    }

    // Test removeHall
    @Test
    public void testRemoveHall_HallNotFound_ThrowsException() {
        when(hallRepo.findById("1")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> cinemaHallService.removeHall("1"));
    }

    // todo: cần xem kĩ lưỡng hơn với ràng buộc trong database.
    @Test
    public void testRemoveHall_Success() {
        when(hallRepo.findById("1")).thenReturn(Optional.of(validHall));

        MyApiResponse response = cinemaHallService.removeHall("1");

        assertEquals("Success", response.getMessage());
        verify(hallSeatService, times(1)).RemoveAllSeatsFromHall("1");
        verify(hallRepo, times(1)).delete(validHall);
    }

//    @Test
//    public void testRemoveHall_WithoutConstraint() {
//        String hallId = "bd52169a-73795f48-9625ea04";
//        hallRepo.deleteById(hallId);
//        assert (!hallRepo.findById(hallId).isPresent());
//    }

    // todo: Không hiểu sao vẫn true
//    @Test
//    public void testRemoveHall_WithConstraint() {
//        String hallId = "336bd103-a0f4802e-875659cb";
//        cinemaHallImpl.removeHall(hallId);
//        assert (!cinemaHallRepository.findById(hallId).isPresent());
//    }

    // Test isExistByName
    @Test
    public void testIsExistByName_Exists_ReturnsTrue() {
        when(hallRepo.existsByName("Hall1")).thenReturn(true);

        boolean result = cinemaHallService.isExistByName("Hall1");

        assertTrue(result);
    }

    @Test
    public void testIsExistByName_NotExists_ReturnsFalse() {
        when(hallRepo.existsByName("Hall1")).thenReturn(false);

        boolean result = cinemaHallService.isExistByName("Hall1");

        assertFalse(result);
    }

    // Test isExistById
    @Test
    public void testIsExistById_Exists_ReturnsTrue() {
        when(hallRepo.existsById("1")).thenReturn(true);

        boolean result = cinemaHallService.isExistById("1");

        assertTrue(result);
    }

    @Test
    public void testIsExistById_NotExists_ReturnsFalse() {
        when(hallRepo.existsById("1")).thenReturn(false);

        boolean result = cinemaHallService.isExistById("1");

        assertFalse(result);
    }

    // Test getAllHalls
    @Test
    public void testGetAllHalls_ReturnsList() {
        CinemaHall hall1 = new CinemaHall();
        CinemaHall hall2 = new CinemaHall();
        List<CinemaHall> halls = Arrays.asList(hall1, hall2);
        when(hallRepo.findAll()).thenReturn(halls);

        List<CinemaHall> result = cinemaHallService.getAllHalls();

        assertEquals(2, result.size());
        assertEquals(halls, result);
    }

    // Test getHallById
    @Test
    public void testGetHallById_HallNotFound_ThrowsException() {
        when(hallRepo.findById("1")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> cinemaHallService.getHallById("1"));
    }

    @Test
    public void testGetHallById_Success() {
        String hallId = "1dfb8524-601aede0-ef0c4c0d";

        CinemaHall result = cinemaHallImpl.getHallById(hallId);

        assertNotNull(result);
    }

    @Test
    public void testGetHallById_Failure() {
        String hallId = "1dfb8524-601aede0-ef0c4c0d11";

        assertThrows(MyNotFoundException.class, () -> cinemaHallImpl.getHallById(hallId));
    }
}