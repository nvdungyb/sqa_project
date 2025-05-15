package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.Genre;
import com.java.be_ticket_booking.model.Movie;
import com.java.be_ticket_booking.repository.GenreReposity;
import com.java.be_ticket_booking.repository.MovieRepo;
import com.java.be_ticket_booking.response.MovieInfoResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.security.InputValidationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    private MovieRepo movieRepo;

    @Mock
    private GenreReposity genreReposity;

    @Mock
    private InputValidationFilter inputValidationFilter;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie movie;
    private Genre genre;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.settitle("Phim 2");
        movie.setDurationInMins(120);
        movie.setGenres(new ArrayList<>());

        genre = new Genre();
        genre.setId(1L);
        genre.setGenre("Comedy");
    }

    @Test
    void testGetMatchingName_Success_TC001() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Movie> movies = List.of(movie);
        when(inputValidationFilter.sanitizeInput("Con")).thenReturn("Con");
        when(inputValidationFilter.checkInput("Con")).thenReturn(true);
        when(movieRepo.findByTitleContaining("Con", pageable)).thenReturn(movies);

        List<MovieInfoResponse> result = movieService.getMatchingName("Con", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals("Phim 2", result.get(0).getTitle());
        verify(movieRepo).findByTitleContaining("Con", pageable);
    }

    @Test
    void testGetMatchingName_EmptyKeyword_TC002() {
        Pageable pageable = PageRequest.of(0, 10);
        when(inputValidationFilter.sanitizeInput("")).thenReturn("");
        when(inputValidationFilter.checkInput("")).thenReturn(true);
        when(movieRepo.findByTitleContaining("", pageable)).thenReturn(Collections.emptyList());

        List<MovieInfoResponse> result = movieService.getMatchingName("", 0, 10);

        assertTrue(result.isEmpty());
        verify(movieRepo).findByTitleContaining("", pageable);
    }

    @Test
    void testGetMatchingName_InvalidKeyword_TC003() {
        when(inputValidationFilter.sanitizeInput("<script>")).thenReturn("<script>");
        when(inputValidationFilter.checkInput("<script>")).thenReturn(false);

        assertThrows(MyBadRequestException.class, () -> movieService.getMatchingName("<script>", 0, 10));
    }

    @Test
    void testSaveMovie_Success_TC004() {
        when(movieRepo.existsByTitle("Phim 2")).thenReturn(false);
        when(movieRepo.save(any(Movie.class))).thenReturn(movie);

        Movie result = movieService.saveMovie(movie);

        assertEquals("Phim 2", result.getTitle());
        verify(movieRepo).save(movie);
    }

    @Test
    void testSaveMovie_TitleExists_TC005() {
        Movie testMovie = new Movie();
        testMovie.settitle("Vệ Binh");
        when(movieRepo.existsByTitle("Vệ Binh")).thenReturn(true);

        assertThrows(MyBadRequestException.class, () -> movieService.saveMovie(testMovie));
    }

    @Test
    void testSaveMovie_NegativeDuration_TC006() {
        Movie testMovie = new Movie();
        testMovie.settitle("Phim 3");
        testMovie.setDurationInMins(-10);
        when(movieRepo.existsByTitle("Phim 3")).thenReturn(false);
        when(movieRepo.save(any(Movie.class))).thenThrow(new MyBadRequestException("Negative duration"));

        assertThrows(MyBadRequestException.class, () -> movieService.saveMovie(testMovie));
    }

    @Test
    void testUpdateMovie_Success_TC007() {
        when(movieRepo.save(any(Movie.class))).thenReturn(movie);

        Movie result = movieService.updateMovie(movie);

        assertEquals("Phim 2", result.getTitle());
        verify(movieRepo).save(movie);
    }

    @Test
    void testUpdateMovie_NegativeDuration_TC008() {
        movie.setDurationInMins(-10);
        when(movieRepo.save(any(Movie.class))).thenThrow(new MyBadRequestException("Negative duration"));

        assertThrows(MyBadRequestException.class, () -> movieService.updateMovie(movie));
    }

    @Test
    void testUpdateMovie_InvalidId_TC009() {
        movie.setId(2L);
        when(movieRepo.save(any(Movie.class))).thenThrow(new MyBadRequestException("Invalid ID"));

        assertThrows(MyBadRequestException.class, () -> movieService.updateMovie(movie));
    }

    @Test
    void testGetMatchingName_NonExistKeyword_TC010() {
        Pageable pageable = PageRequest.of(0, 10);
        when(inputValidationFilter.sanitizeInput("NonExist")).thenReturn("NonExist");
        when(inputValidationFilter.checkInput("NonExist")).thenReturn(true);
        when(movieRepo.findByTitleContaining("NonExist", pageable)).thenReturn(Collections.emptyList());

        List<MovieInfoResponse> result = movieService.getMatchingName("NonExist", 0, 10);

        assertTrue(result.isEmpty());
        verify(movieRepo).findByTitleContaining("NonExist", pageable);
    }

    @Test
    void testGetMovies_Success_TC011() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(movie));
        when(movieRepo.findAll(pageable)).thenReturn(page);

        List<MovieInfoResponse> result = movieService.getMovies(0, 10);

        assertFalse(result.isEmpty());
        assertEquals("Phim 2", result.get(0).getTitle());
        verify(movieRepo).findAll(pageable);
    }

    @Test
    void testGetMovies_EmptyPage_TC012() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(Collections.emptyList());
        when(movieRepo.findAll(pageable)).thenReturn(page);

        List<MovieInfoResponse> result = movieService.getMovies(0, 10);

        assertTrue(result.isEmpty());
        verify(movieRepo).findAll(pageable);
    }

    @Test
    void testGetMatchingGenre_Success_TC013() {
        when(inputValidationFilter.sanitizeInput("com")).thenReturn("com");
        when(inputValidationFilter.checkInput("com")).thenReturn(true);
        when(genreReposity.findByGenreContaining("com")).thenReturn(List.of(genre));
        genre.setTitleList(List.of(movie));

        Object[] result = movieService.getMatchingGenre("com", 0, 10);

        assertEquals(1, result.length);
        assertEquals("Phim 2", ((MovieInfoResponse) result[0]).getTitle());
        verify(genreReposity).findByGenreContaining("com");
    }

    @Test
    void testGetMatchingGenre_EmptyGenres_TC014() {
        when(inputValidationFilter.sanitizeInput("nonexist")).thenReturn("nonexist");
        when(inputValidationFilter.checkInput("nonexist")).thenReturn(true);
        when(genreReposity.findByGenreContaining("nonexist")).thenReturn(Collections.emptyList());

        Object[] result = movieService.getMatchingGenre("nonexist", 0, 10);

        assertEquals(0, result.length);
        verify(genreReposity).findByGenreContaining("nonexist");
    }

    @Test
    void testGetMatchingGenre_InvalidKeyword_TC015() {
        when(inputValidationFilter.sanitizeInput("<script>")).thenReturn("<script>");
        when(inputValidationFilter.checkInput("<script>")).thenReturn(false);

        assertThrows(MyBadRequestException.class, () -> movieService.getMatchingGenre("<script>", 0, 10));
    }

    @Test
    void testGetMovie_Success_TC016() {
        when(movieRepo.findById(1L)).thenReturn(Optional.of(movie));

        MovieInfoResponse result = movieService.getMovie(1L);

        assertEquals("Phim 2", result.getTitle());
        verify(movieRepo).findById(1L);
    }

    @Test
    void testGetMovie_NotFound_TC017() {
        when(movieRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> movieService.getMovie(999L));
    }

    @Test
    void testDeleteMovie_Success_TC018() {
        when(movieRepo.findById(1L)).thenReturn(Optional.of(movie));
        doNothing().when(movieRepo).deleteById(1L);

        MyApiResponse result = movieService.deleteMovie(1L);

        assertEquals("Deleted moive ID 1", result.getMessage());
        verify(movieRepo).deleteById(1L);
    }

    @Test
    void testDeleteMovie_NotFound_TC019() {
        when(movieRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> movieService.deleteMovie(999L));
    }

    @Test
    void testSaveMovieList_SomeTitlesExist_TC020() {
        Movie movie2 = new Movie();
        movie2.settitle("Vệ Binh");
        List<Movie> movies = List.of(movie, movie2);
        lenient().when(movieRepo.existsByTitle("Phim 2")).thenReturn(false);
        lenient().when(movieRepo.existsByTitle("Vệ Binh")).thenReturn(true);
        lenient().when(movieRepo.save(any(Movie.class))).thenReturn(movie);
        lenient().when(genreReposity.findByGenre(anyString())).thenReturn(genre);

        MyApiResponse result = movieService.saveMovieList(movies);

        assertEquals("Success", result.getMessage());
        verify(movieRepo).save(movie);
    }

    @Test
    void testSaveMovieList_NewGenre_TC021() {
        movie.setGenres(List.of(new Genre(null, "Drama")));
        List<Movie> movies = List.of(movie);
        when(movieRepo.existsByTitle("Phim 2")).thenReturn(false);
        when(genreReposity.findByGenre("Drama")).thenReturn(null);
        when(genreReposity.save(any(Genre.class))).thenReturn(genre);
        when(movieRepo.save(any(Movie.class))).thenReturn(movie);

        MyApiResponse result = movieService.saveMovieList(movies);

        assertEquals("Success", result.getMessage());
        verify(genreReposity).save(any(Genre.class));
        verify(movieRepo).save(movie);
    }

    @Test
    void testSaveMovieList_EmptyList_TC022() {
        List<Movie> movies = Collections.emptyList();

        MyApiResponse result = movieService.saveMovieList(movies);

        assertEquals("Success", result.getMessage());
        verify(movieRepo, never()).save(any(Movie.class));
    }
}