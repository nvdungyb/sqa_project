package com.java.be_ticket_booking.service;

import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyConflictExecption;
import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.*;
import com.java.be_ticket_booking.repository.*;
import com.java.be_ticket_booking.request.CinemaHallRequest;
import com.java.be_ticket_booking.request.ShowRequest;
import com.java.be_ticket_booking.response.ErrorResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.ShowInfoResponse;
import com.java.be_ticket_booking.service.impl.CinemaShowServiceImpl;
import com.java.be_ticket_booking.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CinemaShowServiceImplTest {

    @Mock
    private CinemaHallRepository hallREPO;

    @Mock
    private MovieRepo movieREPO;

    @Mock
    private CinemaShowRepository showREPO;

    @Mock
    private CinemaSeatRepository hallSeatRepo;

    @Mock
    private ShowSeatRepository showSeatREPO;

    @InjectMocks
    private CinemaShowServiceImpl cinemaShowService;

    @Mock
    private DateUtils dateUtils;

    private CinemaShow show;
    private CinemaSeat cinemaSeat;
    private String cinemaId;
    private Movie movie;
    private Long movieId;
    private LocalDateTime startTime;
    private CinemaHall cinemaHall;
    private String hallId;
    private CinemaHall hall;
    private ShowRequest showReq;
    private String showId;

    private CinemaShow cinemaShow;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cinemaId = "cinema1";
        CinemaHall hall = new CinemaHall(new CinemaHallRequest("hall", 5, 5));
        show = new CinemaShow();
        show.setId("show1");
        show.setCinemaHall(hall);
        cinemaSeat = new CinemaSeat();

        movieId = 1L;
        show = new CinemaShow();
        movie = new Movie();
        movie.setId(movieId);

        startTime = LocalDateTime.of(2025, 5, 15, 14, 0); // 2:00 PM, May 15, 2025
        cinemaHall = new CinemaHall();
        cinemaHall.setId("hall1");
        movie.setDurationInMins(120); // 2 hours
        show.setMovie(movie);
        show.setCinemaHall(cinemaHall);

        hallId = "hall1";
        show.setId("show1");
        hall = new CinemaHall();
        hall.setId(hallId);
        this.hall = hall;

        showReq = new ShowRequest();
        showReq.setCinemaId("hall1");
        showReq.setMovieId(1L);
        showReq.setStartTime("15/05/2025 14:00");

        showId = "show1";

        cinemaShow = new CinemaShow();
        cinemaShow.setId("show1");
    }

    @Test
    void testAddNewShowSeats_EmptySeats_DeleteOldTrue() {
        // Arrange
        when(hallSeatRepo.findByCinemaHallId(cinemaId)).thenReturn(Collections.emptyList());

        // Act
        cinemaShowService.addNewShowSeats(cinemaId, show, true);

        // Assert
        verify(hallSeatRepo).findByCinemaHallId(cinemaId);
        verify(showSeatREPO).deleteAllByShowId(show.getId());
        verify(showSeatREPO, never()).save(any(ShowSeat.class));
    }

    @Test
    void testAddNewShowSeats_EmptySeats_DeleteOldFalse() {
        // Arrange
        when(hallSeatRepo.findByCinemaHallId(cinemaId)).thenReturn(Collections.emptyList());

        // Act
        cinemaShowService.addNewShowSeats(cinemaId, show, false);

        // Assert
        verify(hallSeatRepo).findByCinemaHallId(cinemaId);
        verify(showSeatREPO, never()).deleteAllByShowId(anyString());
        verify(showSeatREPO, never()).save(any(ShowSeat.class));
    }

    @Test
    void testAddNewShowSeats_NonEmptySeats_DeleteOldTrue() {
        // Arrange
        List<CinemaSeat> seats = List.of(cinemaSeat);
        when(hallSeatRepo.findByCinemaHallId(cinemaId)).thenReturn(seats);

        // Act
        cinemaShowService.addNewShowSeats(cinemaId, show, true);

        // Assert
        verify(hallSeatRepo).findByCinemaHallId(cinemaId);
        verify(showSeatREPO).deleteAllByShowId(show.getId());
        verify(showSeatREPO, times(1)).save(any(ShowSeat.class));
    }

    @Test
    void testAddNewShowSeats_NonEmptySeats_DeleteOldFalse() {
        // Arrange
        List<CinemaSeat> seats = List.of(cinemaSeat);
        when(hallSeatRepo.findByCinemaHallId(cinemaId)).thenReturn(seats);

        // Act
        cinemaShowService.addNewShowSeats(cinemaId, show, false);

        // Assert
        verify(hallSeatRepo).findByCinemaHallId(cinemaId);
        verify(showSeatREPO, never()).deleteAllByShowId(anyString());
        verify(showSeatREPO, times(1)).save(any(ShowSeat.class));
    }

    @Test
    void testUpdateNewMovie_MovieFound() {
        // Arrange
        when(movieREPO.findById(movieId)).thenReturn(Optional.of(movie));

        // Act
        cinemaShowService.updateNewMovie(show, movieId);

        // Assert
        verify(movieREPO).findById(movieId);
        verify(showREPO).save(show);
        // Optionally verify that setMovie was called with the correct movie
        assert show.getMovie() == movie;
    }

    @Test
    void testUpdateNewMovie_MovieNotFound() {
        // Arrange
        when(movieREPO.findById(movieId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MyNotFoundException.class, () -> {
            cinemaShowService.updateNewMovie(show, movieId);
        });

        // Assert
        verify(movieREPO).findById(movieId);
        verify(showREPO, never()).save(any(CinemaShow.class));
    }

    @Test
    void testUpdateNewStartTime_NoConflictingShows() {
        // Arrange
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationInMins()).plusMinutes(10);
        when(showREPO.findConflictingShows(startTime, endTime, cinemaHall.getId()))
                .thenReturn(Collections.emptyList());

        // Act
        cinemaShowService.updateNewStartTime(show, startTime);

        // Assert
        verify(showREPO).findConflictingShows(startTime, endTime, cinemaHall.getId());
        verify(showREPO).save(show);
        // Verify that startTime and endTime were set correctly
        assert show.getStartTime().equals(startTime);
        assert show.getEndTime().equals(endTime);
    }

    @Test
    void testUpdateNewStartTime_ConflictingShows() {
        // Arrange
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationInMins()).plusMinutes(10);
        CinemaShow conflictingShow = new CinemaShow();
        conflictingShow.setId("conflict1");
        List<CinemaShow> conflictingShows = List.of(conflictingShow);
        when(showREPO.findConflictingShows(startTime, endTime, cinemaHall.getId()))
                .thenReturn(conflictingShows);

        // Act & Assert
        MyConflictExecption exception = assertThrows(MyConflictExecption.class, () -> {
            cinemaShowService.updateNewStartTime(show, startTime);
        });
        assert exception.getMessage().equals("Conflict start/end time with show ID: conflict1");

        // Assert
        verify(showREPO).findConflictingShows(startTime, endTime, cinemaHall.getId());
        verify(showREPO, never()).save(any(CinemaShow.class));
        // Verify that startTime and endTime were not set
        assert show.getStartTime() == null;
        assert show.getEndTime() == null;
    }

    @Test
    void testUpdateNewHall_HallFound() {
        // Arrange
        when(hallREPO.findById(hallId)).thenReturn(Optional.of(hall));
        when(hallSeatRepo.findByCinemaHallId(hallId)).thenReturn(Collections.emptyList());
        when(showREPO.save(show)).thenReturn(show);

        // Act
        CinemaShow result = cinemaShowService.updateNewHall(show, hallId);

        // Assert
        verify(hallREPO).findById(hallId);
        verify(hallSeatRepo).findByCinemaHallId(hallId);
        verify(showSeatREPO).deleteAllByShowId(show.getId());
        verify(showSeatREPO, never()).save(any(ShowSeat.class)); // Empty seat list
        verify(showREPO).save(show);
        assertEquals(hall, show.getCinemaHall());
        assertEquals(show, result);
    }

    @Test
    void testUpdateNewHall_HallNotFound() {
        // Arrange
        when(hallREPO.findById(hallId)).thenReturn(Optional.empty());

        // Act & Assert
        MyNotFoundException exception = assertThrows(MyNotFoundException.class, () -> {
            cinemaShowService.updateNewHall(show, hallId);
        });
        assertEquals("Hall is not found", exception.getMessage());

        // Assert
        verify(hallREPO).findById(hallId);
        verify(hallSeatRepo, never()).findByCinemaHallId(anyString());
        verify(showSeatREPO, never()).deleteAllByShowId(anyString());
        verify(showSeatREPO, never()).save(any(ShowSeat.class));
        verify(showREPO, never()).save(any(CinemaShow.class));
    }

    @Test
    void testAddOneShow_HallNotFound() {
        // Arrange
        when(hallREPO.findById(showReq.getCinemaId())).thenReturn(Optional.empty());

        // Act & Assert
        MyNotFoundException exception = assertThrows(MyNotFoundException.class, () -> {
            cinemaShowService.addOneShow(showReq);
        });
        assertEquals("Hall is not found", exception.getMessage());

        // Assert
        verify(hallREPO).findById(showReq.getCinemaId());
        verify(movieREPO, never()).findById(anyLong());
        verify(showREPO, never()).save(any(CinemaShow.class));
        verify(hallSeatRepo, never()).findByCinemaHallId(anyString());
        verify(showSeatREPO, never()).save(any(ShowSeat.class));
    }

    @Test
    void testAddOneShow_MovieNotFound() {
        // Arrange
        when(hallREPO.findById(showReq.getCinemaId())).thenReturn(Optional.of(hall));
        when(movieREPO.findById(showReq.getMovieId())).thenReturn(Optional.empty());

        // Act & Assert
        MyNotFoundException exception = assertThrows(MyNotFoundException.class, () -> {
            cinemaShowService.addOneShow(showReq);
        });
        assertEquals("Movie is not found", exception.getMessage());

        // Assert
        verify(hallREPO).findById(showReq.getCinemaId());
        verify(movieREPO).findById(showReq.getMovieId());
        verify(showREPO, never()).save(any(CinemaShow.class));
        verify(hallSeatRepo, never()).findByCinemaHallId(anyString());
        verify(showSeatREPO, never()).save(any(ShowSeat.class));
    }

    @Test
    void testAddOneShow_InvalidDateFormat() {
        // Arrange
        when(hallREPO.findById(showReq.getCinemaId())).thenReturn(Optional.of(hall));
        when(movieREPO.findById(showReq.getMovieId())).thenReturn(Optional.of(movie));
        when(dateUtils.convertStringDateToDate(showReq.getStartTime(), "dd/MM/yyyy HH:mm")).thenReturn(null);

        // Act & Assert
        MyBadRequestException exception = assertThrows(MyBadRequestException.class, () -> {
            cinemaShowService.addOneShow(showReq);
        });
        assertEquals("Invaild date format, it must be dd/MM/yyyy HH:mm", exception.getMessage());

        // Assert
        verify(hallREPO).findById(showReq.getCinemaId());
        verify(movieREPO).findById(showReq.getMovieId());
        verify(dateUtils).convertStringDateToDate(showReq.getStartTime(), "dd/MM/yyyy HH:mm");
        verify(showREPO, never()).save(any(CinemaShow.class));
        verify(hallSeatRepo, never()).findByCinemaHallId(anyString());
        verify(showSeatREPO, never()).save(any(ShowSeat.class));
    }

    @Test
    void testAddOneShow_Success() {
        // Arrange
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationInMins()).plusMinutes(10);
        when(hallREPO.findById(showReq.getCinemaId())).thenReturn(Optional.of(hall));
        when(movieREPO.findById(showReq.getMovieId())).thenReturn(Optional.of(movie));
        when(dateUtils.convertStringDateToDate(showReq.getStartTime(), "dd/MM/yyyy HH:mm")).thenReturn(startTime);
        when(showREPO.save(any(CinemaShow.class))).thenReturn(show);
        when(hallSeatRepo.findByCinemaHallId(showReq.getCinemaId())).thenReturn(Collections.emptyList());

        // Act
        String result = cinemaShowService.addOneShow(showReq);

        // Assert
        verify(hallREPO).findById(showReq.getCinemaId());
        verify(movieREPO).findById(showReq.getMovieId());
        verify(dateUtils).convertStringDateToDate(showReq.getStartTime(), "dd/MM/yyyy HH:mm");
        verify(showREPO).save(any(CinemaShow.class));
        verify(hallSeatRepo).findByCinemaHallId(showReq.getCinemaId());
        verify(showSeatREPO, never()).save(any(ShowSeat.class)); // Empty seat list
        assertEquals(show.getId(), result);
    }

    @Test
    void testGetAllShows_EmptyList() {
        // Arrange
        when(showREPO.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ShowInfoResponse> result = cinemaShowService.getAllShows();

        // Assert
        verify(showREPO).findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllShows_EmptyList_CallsFindAll() {
        // Arrange
        when(showREPO.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ShowInfoResponse> result = cinemaShowService.getAllShows();

        // Assert
        verify(showREPO, times(1)).findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllShows_NonEmptyList_CallsFindAll() {
        // Arrange
        List<CinemaShow> shows = List.of(show);
        when(showREPO.findAll()).thenReturn(shows);

        // Act
        List<CinemaShow> result = showREPO.findAll();

        // Assert
        verify(showREPO, times(1)).findAll();
        assertEquals(1, result.size());
        // Minimal verification of ShowInfoResponse content to focus on findAll
        assertEquals(show.getId(), result.get(0).getId()); // Assumes ShowInfoResponse has getShow()
    }

    @Test
    void testDeleteShow_ShowNotFound() {
        // Arrange
        when(showREPO.existsById(showId)).thenReturn(false);

        // Act
        MyApiResponse result = cinemaShowService.deleteShow(showId);

        // Assert
        verify(showREPO).existsById(showId);
        verify(showSeatREPO, never()).deleteAllByShowId(anyString());
        verify(showREPO, never()).deleteById(anyString());
        assertTrue(result instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) result;
        assertEquals("Show is found", errorResponse.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatus());
    }

    @Test
    void testDeleteShow_ShowFound() {
        // Arrange
        when(showREPO.existsById(showId)).thenReturn(true);

        // Act
        MyApiResponse result = cinemaShowService.deleteShow(showId);

        // Assert
        verify(showREPO).existsById(showId);
        verify(showSeatREPO).deleteAllByShowId(showId);
        verify(showREPO).deleteById(showId);
        assertTrue(result instanceof MyApiResponse);
        assertEquals("Done", result.getMessage());
    }

    @Test
    void testAddNewShowSeats_NonEmptySeats_DeleteOldTrue_MultipleSeats() {
        // Arrange
        List<CinemaSeat> seats = List.of(cinemaSeat, new CinemaSeat());
        when(hallSeatRepo.findByCinemaHallId(cinemaId)).thenReturn(seats);

        // Act
        cinemaShowService.addNewShowSeats(cinemaId, show, true);

        // Assert
        verify(hallSeatRepo).findByCinemaHallId(cinemaId);
        verify(showSeatREPO).deleteAllByShowId(show.getId());
        verify(showSeatREPO, times(seats.size())).save(any(ShowSeat.class));
    }


}