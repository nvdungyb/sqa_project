package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.Genre;
import com.java.be_ticket_booking.repository.GenreReposity;
import com.java.be_ticket_booking.response.MyApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreReposity genreReposity;

    @InjectMocks
    private GenreServiceImpl genreService;

    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = new Genre();
        genre.setId(1L);
        genre.setGenre("Comedy");
    }

    @Test
    void testGetGenres_Success_TC001() {
        when(genreReposity.findAll()).thenReturn(List.of(genre));

        List<Genre> result = genreService.getGenres();

        assertFalse(result.isEmpty());
        assertEquals("Comedy", result.get(0).getGenre());
        verify(genreReposity).findAll();
    }

    @Test
    void testGetGenres_EmptyList_TC002() {
        when(genreReposity.findAll()).thenReturn(Collections.emptyList());

        List<Genre> result = genreService.getGenres();

        assertTrue(result.isEmpty());
        verify(genreReposity).findAll();
    }

    @Test
    void testSaveGenre_Success_TC003() {
        when(genreReposity.existsByGenre("Comedy")).thenReturn(false);
        when(genreReposity.save(any(Genre.class))).thenReturn(genre);

        Genre result = genreService.saveGenre(genre);

        assertEquals("Comedy", result.getGenre());
        verify(genreReposity).save(genre);
    }

    @Test
    void testSaveGenre_GenreExists_TC004() {
        when(genreReposity.existsByGenre("Comedy")).thenReturn(true);

        assertThrows(MyBadRequestException.class, () -> genreService.saveGenre(genre));
    }

    @Test
    void testUpdateGenre_Success_TC005() {
        Genre updatedGenre = new Genre();
        updatedGenre.setId(1L);
        updatedGenre.setGenre("Drama");
        when(genreReposity.existsById(1L)).thenReturn(true);
        when(genreReposity.existsByGenre("Drama")).thenReturn(false);
        when(genreReposity.save(any(Genre.class))).thenReturn(updatedGenre);

        Genre result = genreService.updateGenre(updatedGenre);

        assertEquals("Drama", result.getGenre());
        verify(genreReposity).save(updatedGenre);
    }

    @Test
    void testUpdateGenre_IdNotFound_TC006() {
        Genre updatedGenre = new Genre();
        updatedGenre.setId(999L);
        updatedGenre.setGenre("Drama");
        when(genreReposity.existsById(999L)).thenReturn(false);

        assertThrows(MyNotFoundException.class, () -> genreService.updateGenre(updatedGenre));
    }

    @Test
    void testUpdateGenre_GenreExists_TC007() {
        Genre updatedGenre = new Genre();
        updatedGenre.setId(1L);
        updatedGenre.setGenre("Comedy");
        when(genreReposity.existsById(1L)).thenReturn(true);
        when(genreReposity.existsByGenre("Comedy")).thenReturn(true);

        assertThrows(MyBadRequestException.class, () -> genreService.updateGenre(updatedGenre));
    }

    @Test
    void testGetGenre_Success_TC008() {
        when(genreReposity.findById(1L)).thenReturn(Optional.of(genre));

        Genre result = genreService.getGenre(1L);

        assertEquals("Comedy", result.getGenre());
        verify(genreReposity).findById(1L);
    }

    @Test
    void testGetGenre_NotFound_TC009() {
        when(genreReposity.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () -> genreService.getGenre(999L));
    }

    @Test
    void testDeleteGenre_Success_TC010() {
        when(genreReposity.existsById(1L)).thenReturn(true);
        doNothing().when(genreReposity).deleteById(1L);

        MyApiResponse result = genreService.deleteGenre(1L);

        assertEquals("Delete genre ID 1", result.getMessage());
        verify(genreReposity).deleteById(1L);
    }

    @Test
    void testDeleteGenre_NotFound_TC011() {
        when(genreReposity.existsById(999L)).thenReturn(false);

        assertThrows(MyNotFoundException.class, () -> genreService.deleteGenre(999L));
    }

    @Test
    void testSaveListGenres_SomeGenresExist_TC012() {
        Genre genre2 = new Genre();
        genre2.setGenre("Drama");
        List<Genre> genres = List.of(genre, genre2);
        lenient().when(genreReposity.existsByGenre("Comedy")).thenReturn(true);
        lenient().when(genreReposity.existsByGenre("Drama")).thenReturn(false);
        lenient().when(genreReposity.save(any(Genre.class))).thenReturn(genre2);

        MyApiResponse result = genreService.saveListGenres(genres);

        assertEquals("Success", result.getMessage());
        verify(genreReposity).save(genre2);
    }

    @Test
    void testSaveListGenres_EmptyList_TC013() {
        List<Genre> genres = Collections.emptyList();

        MyApiResponse result = genreService.saveListGenres(genres);

        assertEquals("Success", result.getMessage());
        verify(genreReposity, never()).save(any(Genre.class));
    }

    @Test
    void testSaveGenre_LongName_Success_TC016() {
        Genre longNameGenre = new Genre();
        longNameGenre.setGenre("VeryLongGenreNameWithManyCharactersToTestBoundary");
        when(genreReposity.existsByGenre("VeryLongGenreNameWithManyCharactersToTestBoundary")).thenReturn(false);
        when(genreReposity.save(any(Genre.class))).thenReturn(longNameGenre);

        Genre result = genreService.saveGenre(longNameGenre);

        assertEquals("VeryLongGenreNameWithManyCharactersToTestBoundary", result.getGenre());
        verify(genreReposity).save(longNameGenre);
    }

    @Test
    void testSaveGenre_NullName_Fail_TC017() {
        Genre nullNameGenre = new Genre();
        nullNameGenre.setGenre(null);
        assertThrows(MyBadRequestException.class, () -> genreService.saveGenre(nullNameGenre));
    }

    @Test
    void testUpdateGenre_SpecialName_Success_TC018() {
        Genre specialNameGenre = new Genre();
        specialNameGenre.setId(1L);
        specialNameGenre.setGenre("Hài Kịch");
        when(genreReposity.existsById(1L)).thenReturn(true);
        when(genreReposity.existsByGenre("Hài Kịch")).thenReturn(false);
        when(genreReposity.save(any(Genre.class))).thenReturn(specialNameGenre);

        Genre result = genreService.updateGenre(specialNameGenre);

        assertEquals("Hài Kịch", result.getGenre());
        verify(genreReposity).save(specialNameGenre);
    }

    @Test
    void testUpdateGenre_NullId_Fail_TC019() {
        Genre nullIdGenre = new Genre();
        nullIdGenre.setId(null);
        nullIdGenre.setGenre("Drama");
        assertThrows(MyBadRequestException.class, () -> genreService.updateGenre(nullIdGenre));
    }

    @Test
    void testSaveListGenres_LargeList_Success_TC020() {
        List<Genre> largeList = IntStream.range(0, 10)
                .mapToObj(i -> {
                    Genre g = new Genre();
                    g.setGenre("Genre" + i);
                    return g;
                })
                .collect(Collectors.toList());
        lenient().when(genreReposity.existsByGenre(anyString())).thenReturn(false);
        lenient().when(genreReposity.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MyApiResponse result = genreService.saveListGenres(largeList);

        assertEquals("Success", result.getMessage());
        verify(genreReposity, times(10)).save(any(Genre.class));
    }

    @Test
    void testDeleteGenre_NullId_Fail_TC021() {
        assertThrows(MyBadRequestException.class, () -> genreService.deleteGenre(null));
    }
}