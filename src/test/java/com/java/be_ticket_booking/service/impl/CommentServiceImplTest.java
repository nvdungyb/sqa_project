package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.*;
import com.java.be_ticket_booking.model.enumModel.BookingStatus;
import com.java.be_ticket_booking.repository.*;
import com.java.be_ticket_booking.request.AddCommentRequest;
import com.java.be_ticket_booking.request.EditCommentRequest;
import com.java.be_ticket_booking.response.CommentResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.service.impl.CommentServiceImpl;
import com.java.be_ticket_booking.service.impl.InputValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private MovieRepo movieRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private CommentRepository commentRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private InputValidationServiceImpl validService;

    private Account mockUser;
    private Movie mockMovie;
    private Comment mockComment;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new Account();
        mockUser.setId("user1");
        mockUser.setUsername("testuser");

        mockMovie = new Movie();
        mockMovie.setId(1L);

        mockComment = new Comment();
        mockComment.setId("cmt1");
        mockComment.setUser(mockUser);
        mockComment.setMovie(mockMovie);
    }

    @Test
    void addComment_shouldSaveComment() {
        AddCommentRequest req = new AddCommentRequest();
        req.setMovieId(1L);
        req.setRatedStars(4);
        req.setComment("Great movie");

        when(userRepo.getByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(movieRepo.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(bookingRepo.findByUserIdAndMovieIdAndStatus("user1", 1L, BookingStatus.BOOKED))
                .thenReturn(Optional.of(new Booking()));
        when(commentRepo.existsByUserIdAndMovieId("user1", 1L)).thenReturn(false);
        when(commentRepo.save(any())).thenReturn(mockComment);

        CommentResponse res = commentService.addComment("testuser", req);
        assertNotNull(res);
    }

    @Test
    void editComment_shouldUpdateComment() {
        EditCommentRequest req = new EditCommentRequest();
        req.setComment("Updated");
        req.setRatingStars(5);

        when(commentRepo.findById("cmt1")).thenReturn(Optional.of(mockComment));
        when(validService.sanitizeInput("Updated")).thenReturn("Updated");
        when(commentRepo.save(any())).thenReturn(mockComment);

        CommentResponse res = commentService.editComment("testuser", "cmt1", req);
        assertNotNull(res);
    }

    @Test
    void deleteCommentById_shouldDeleteComment() {
        when(commentRepo.findById("cmt1")).thenReturn(Optional.of(mockComment));
        MyApiResponse res = commentService.deleteCommentById("cmt1");
        assertEquals("Done", res.getMessage());
    }

    @Test
    void getAllComments_shouldReturnList() {
        when(commentRepo.findAll()).thenReturn(List.of(mockComment));
        List<CommentResponse> res = commentService.getAllComments();
        assertEquals(1, res.size());
    }

    @Test
    void getAllCommentsFromusername_validUsername_shouldReturnList() {
        String username = "user1";
        Account mockUser = new Account();
        mockUser.setId("user-id-1");
        mockUser.setUsername(username);

        Comment comment = new Comment();
        comment.setUser(mockUser);

        List<Comment> comments = new ArrayList<>();
        comments.add(comment);

        when(userRepo.getByUsername(username)).thenReturn(Optional.of(mockUser));
        when(commentRepo.findAllByUserId(mockUser.getId())).thenReturn(comments);

        List<CommentResponse> result = commentService.getAllCommentsFromusername(username);

        assertNotNull(result);
        assertEquals(comments.size(), result.size());
        verify(userRepo).getByUsername(username);
        verify(commentRepo).findAllByUserId(mockUser.getId());
    }

    @Test
    void deleteCommentByUsername_wrongUser_shouldThrow() {
        Account anotherUser = new Account();
        anotherUser.setUsername("another");
        mockComment.setUser(anotherUser);

        when(commentRepo.findById("cmt1")).thenReturn(Optional.of(mockComment));

        assertThrows(MyBadRequestException.class, () -> commentService.deleteCommentByUsername("testuser", "cmt1"));
    }

    @Test
    void addComment_invalidRating_shouldThrow() {
        String username = "testuser";
        long movieId = 1L;

        Account mockUser = new Account();  // tạo đối tượng giả
        mockUser.setId("user123");

        Movie mockMovie = new Movie();
        mockMovie.setId(movieId);

        AddCommentRequest request = new AddCommentRequest();
        request.setComment("Great movie!");
        request.setRatedStars(10); // invalid rating
        // nếu AddCommentRequest chưa có setter, cần thêm như bạn đã làm với EditCommentRequest

        // Mock các bước đi trước validation rating
        when(userRepo.getByUsername(username)).thenReturn(Optional.of(mockUser));
        when(movieRepo.findById(movieId)).thenReturn(Optional.of(mockMovie));
        when(bookingRepo.findByUserIdAndMovieIdAndStatus("user123", movieId, BookingStatus.BOOKED))
                .thenReturn(Optional.of(new Booking()));
        when(commentRepo.existsByUserIdAndMovieId("user123", movieId)).thenReturn(false);

        assertThrows(MyBadRequestException.class, () -> commentService.addComment(username, request));
    }
    @Test
    void getAllCommentsFromMovieId_shouldReturnCommentResponses() {
        long movieId = 1L;
        Comment comment = new Comment();
        comment.setId("c1");
        when(commentRepo.findAllByMovieId(movieId)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getAllCommentsFromMovieId(movieId);

        assertEquals(1, responses.size());
        assertEquals("c1", responses.get(0).getCommentId());
    }
    @Test
    void getAllCommentsFromUserId_shouldReturnCommentResponses() {
        String userId = "u1";
        Comment comment = new Comment();
        comment.setId("c1");
        when(commentRepo.findAllByUserId(userId)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getAllCommentsFromUserId(userId);

        assertEquals(1, responses.size());
        assertEquals("c1", responses.get(0).getCommentId());
    }
    @Test
    void deleteCommentByUsername_shouldDeleteCommentSuccessfully() {
        String username = "user1";
        Comment comment = new Comment();
        Account user = new Account();
        user.setUsername(username);
        comment.setUser(user);
        comment.setMovie(new Movie());

        when(commentRepo.findById("c1")).thenReturn(Optional.of(comment));
        when(userRepo.getByUsername(username)).thenReturn(Optional.of(user));

        MyApiResponse response = commentService.deleteCommentByUsername(username, "c1");

        assertEquals("Done", response.getMessage());
        verify(commentRepo).delete(comment);
    }
    @Test
    void addLike_notImplemented_shouldReturnNull() {
        String username = "user1";
        long movieId = 123L;

        MyApiResponse response = commentService.addLike(username, movieId);

        assertNull(response, "addLike should return null as not implemented yet");
    }

    @Test
    void addDisLike_notImplemented_shouldReturnNull() {
        String username = "user1";
        long movieId = 123L;

        MyApiResponse response = commentService.addDisLike(username, movieId);

        assertNull(response, "addDisLike should return null as not implemented yet");
    }

}
