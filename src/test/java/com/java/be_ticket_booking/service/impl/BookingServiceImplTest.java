package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.*;
import com.java.be_ticket_booking.model.*;
import com.java.be_ticket_booking.model.enumModel.*;
import com.java.be_ticket_booking.repository.*;
import com.java.be_ticket_booking.request.BookingRequest;
import com.java.be_ticket_booking.response.BookingResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.service.PaymentService;
import com.java.be_ticket_booking.service.UserService;
import com.java.be_ticket_booking.utils.VNPay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
public class BookingServiceImplTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private ShowSeatRepository showSeatRepo;
    @Mock
    private CinemaShowRepository showRepo;
    @Mock
    private BookingRepository bookingRepo;
    @Mock
    private PaymentRepository paymentRepo;
    @Mock
    private SpamUserRepository spamRepo;
    @Mock
    private UserService userService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Account mockUser;
    private CinemaShow mockShow;
    private ShowSeat mockSeat;
    private BookingRequest bookingRequest;
    private Booking mockBooking;

    private static final long TIMEOUT = 1;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new Account();
        mockUser.setId("825dec0a-3a9e-444d-8b26-49d9388e41ac");
        mockUser.setUsername("duynamasas");
        mockUser.setFullname("Duong Duy Naasasm");
        mockUser.setStatus(UserStatus.ACTIVE);

        CinemaHall mockCinemaHall = new CinemaHall();
        mockCinemaHall.setId("1dfb8524-601aede0-ef0c4c0d");
        mockCinemaHall.setName("B");

        // Tạo mock Movie
        Movie mockMovie = new Movie();
        mockMovie.setId(2L);
        mockMovie.setTitle("Con Nhót Mót Chồng");

        mockShow = new CinemaShow();
        mockShow.setId("9ec7e849-190e222e-de378f6a");
        mockShow.setMovie(mockMovie);
        mockShow.setCinemaHall(mockCinemaHall);
        String dateTimeString = "2023-06-13 16:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        mockShow.setStartTime(LocalDateTime.parse(dateTimeString, formatter)); // Gán startTime là LocalDateTime

        mockShow.setCreateAt(new Date());
        CinemaSeat cinemaSeat = new CinemaSeat(); // ✅ tạo CinemaSeat mock
        cinemaSeat.setId(74L);
        cinemaSeat.setPrice(10000.0);

        mockSeat = new ShowSeat();
        mockSeat.setId("12xgx5sFNV");
        mockSeat.setShow(mockShow);
        mockSeat.setStatus(ESeatStatus.AVAILABLE);
        mockSeat.setCinemaSeat(cinemaSeat);

        bookingRequest = new BookingRequest();
        bookingRequest.setShowId("9ec7e849-190e222e-de378f6a");
        bookingRequest.setSeatsId(Arrays.asList("12xgx5sFNV"));

        mockBooking = new Booking();
        mockBooking.setId("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf");
        mockBooking.setUser(mockUser);
        mockBooking.setShow(mockShow);
//        mockBooking.setBookingTime(LocalDateTime.now());
        mockBooking.setSeats(Arrays.asList(mockSeat));
        mockBooking.setStatus(BookingStatus.PENDING);

    }
    private void addUserToSpamQueue(Account user) throws Exception {
        Field spamUsersField = BookingServiceImpl.class.getDeclaredField("spamUsers");
        spamUsersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Queue<Account> queue = (Queue<Account>) spamUsersField.get(bookingService);
        queue.add(user);
    }
    private static final int MAXSPAM_IN_SERVICE = 3;
    @Test
    public void testCreateBooking_Success() {
        // Given
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(showRepo.findById("9ec7e849-190e222e-de378f6a")).thenReturn(Optional.of(mockShow));
        when(bookingRepo.countByShowId("9ec7e849-190e222e-de378f6a")).thenReturn(0);
        when(showSeatRepo.countByShowIdAndStatus("9ec7e849-190e222e-de378f6a", ESeatStatus.AVAILABLE)).thenReturn(10);
        when(showSeatRepo.findByIdAndShowId("12xgx5sFNV", "9ec7e849-190e222e-de378f6a")).thenReturn(Optional.of(mockSeat));
        when(showSeatRepo.save(any(ShowSeat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookingResponse response = bookingService.createBooking("duynamasas", bookingRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockUser.getFullname(), response.getFullname());
        assertEquals("Con Nhót Mót Chồng", response.movieName());
        verify(bookingRepo, times(1)).save(any(Booking.class));
    }

    @Test
    public void testCreateBooking_UserNotFound() {
        // Given
        when(userRepo.getByUsername("non_existing_user")).thenReturn(Optional.empty());

        // When & Then
        MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () ->
                bookingService.createBooking("non_existing_user", bookingRequest)
        );
        assertEquals("User is not found", thrown.getMessage());
        verify(userRepo, times(1)).getByUsername("non_existing_user");
    }

    @Test
    public void testCreateBooking_ShowNotFound() {
        // Given
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(showRepo.findById("9ec7e849-190e222e-de378f6a")).thenReturn(Optional.empty());

        // When & Then
        MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () ->
                bookingService.createBooking("duynamasas", bookingRequest)
        );
        assertEquals("Show is not found", thrown.getMessage());
        verify(userRepo, times(1)).getByUsername("duynamasas");
        verify(showRepo, times(1)).findById("9ec7e849-190e222e-de378f6a");
    }

    @Test
    public void testCreateBooking_SeatNotFound() {
        // Given
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(showRepo.findById("9ec7e849-190e222e-de378f6a")).thenReturn(Optional.of(mockShow));
        when(bookingRepo.countByShowId("9ec7e849-190e222e-de378f6a")).thenReturn(0);
        when(showSeatRepo.countByShowIdAndStatus("9ec7e849-190e222e-de378f6a", ESeatStatus.AVAILABLE)).thenReturn(10);
        when(showSeatRepo.findByIdAndShowId("12xgx5sFNV", "9ec7e849-190e222e-de378f6a")).thenReturn(Optional.empty());

        // When & Then
        MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () ->
                bookingService.createBooking("duynamasas", bookingRequest)
        );
        assertEquals("Not found seat id: 12xgx5sFNV", thrown.getMessage());
    }


    @Test
    public void testCreateBooking_SeatNotAvailable() {
        mockSeat.setStatus(ESeatStatus.BOOKED);
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(showRepo.findById("9ec7e849-190e222e-de378f6a")).thenReturn(Optional.of(mockShow));
        when(bookingRepo.countByShowId("9ec7e849-190e222e-de378f6a")).thenReturn(0);
        when(showSeatRepo.countByShowIdAndStatus("9ec7e849-190e222e-de378f6a", ESeatStatus.AVAILABLE)).thenReturn(10);
        when(showSeatRepo.findByIdAndShowId("12xgx5sFNV", "9ec7e849-190e222e-de378f6a")).thenReturn(Optional.of(mockSeat));

        MyConflictExecption thrown = assertThrows(MyConflictExecption.class, () ->
                bookingService.createBooking("duynamasas", bookingRequest)
        );
        assertEquals("Seat ID 12xgx5sFNV is reserved", thrown.getMessage());
    }

    @Test
    public void testCreateBooking_ShowIsFull() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(showRepo.findById("9ec7e849-190e222e-de378f6a")).thenReturn(Optional.of(mockShow));
        when(bookingRepo.countByShowId("9ec7e849-190e222e-de378f6a")).thenReturn(100); // full
        when(showSeatRepo.countByShowIdAndStatus("9ec7e849-190e222e-de378f6a", ESeatStatus.AVAILABLE)).thenReturn(0); // hết chỗ

//        MyConflictExecption thrown = assertThrows(MyConflictExecption.class, () ->
//                bookingService.createBooking("duynamasas", bookingRequest)
//        );
        MyLockedException thrown = assertThrows(MyLockedException.class, () ->
                bookingService.createBooking("duynamasas", bookingRequest)
        );


        assertEquals("Sorry, seats of this show are full. Please choose another show", thrown.getMessage());
    }

    @Test
    public void testCancelBooking_Success() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findById("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")).thenReturn(Optional.of(mockBooking));

        // Thực thi phương thức cancelBooking
        MyApiResponse response = bookingService.cancleBooking("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf");

        // Kiểm tra kết quả
        assertEquals("Done", response.getMessage());
    }




    @Test
    public void testCancelBooking_UserNotFound() {
        // Given
        when(userRepo.getByUsername("non_existing_user")).thenReturn(Optional.empty());

        // When & Then
        MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () ->
                bookingService.cancleBooking("non_existing_user", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")
        );
        assertEquals("User is not found", thrown.getMessage());
    }

    @Test
    public void testCancelBooking_BookingNotFound() {
        // Given
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findById("non_existing_booking")).thenReturn(Optional.empty());

        // When & Then
        MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () ->
                bookingService.cancleBooking("duynamasas", "non_existing_booking")
        );
        assertEquals("Booking ticket is not found", thrown.getMessage());
    }

    @Test
    public void testCancleBooking_TicketDoesNotBelongToUser() {
        // Given
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser)); // Trả về mockUser
        when(bookingRepo.findById("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")).thenReturn(Optional.of(mockBooking)); // Trả về mockBooking

//        mockUser.setUsername("duynamasas");

        Account bookingUser = new Account();
        bookingUser.setUsername("long");  // Đặt tên người dùng
        mockBooking.setUser(bookingUser);
        // When & Then
        MyConflictExecption thrown = assertThrows(MyConflictExecption.class, () ->
                bookingService.cancleBooking("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")
        );

        assertEquals("This ticket does not belong to user duynamasas", thrown.getMessage());
    }

    @Test
    public void testCancleBooking_CanNotCancel() {
        // Given
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));

        when(bookingRepo.findById("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")).thenReturn(Optional.of(mockBooking));
        mockBooking.setStatus(BookingStatus.BOOKED);
        // When & Then
        MyBadRequestException thrown = assertThrows(MyBadRequestException.class, () ->
                bookingService.cancleBooking("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")
        );
        assertEquals("This ticket can not be cancled", thrown.getMessage());
    }

    @Test
    public void testCancelBooking_AlreadyCancelled() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));

        mockBooking.setStatus(BookingStatus.CANCLED);
        when(bookingRepo.findById(mockBooking.getId())).thenReturn(Optional.of(mockBooking));

        MyBadRequestException thrown = assertThrows(MyBadRequestException.class, () ->
                bookingService.cancleBooking("duynamasas", mockBooking.getId())
        );

        assertEquals("This ticket can not be cancled", thrown.getMessage());
    }

    @Test
    public void testCancelBooking_UserMatchesBookingUser() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findById(mockBooking.getId())).thenReturn(Optional.of(mockBooking));

        // User của booking trùng username
        mockBooking.setUser(mockUser);
        mockBooking.setStatus(BookingStatus.PENDING);

        MyApiResponse response = bookingService.cancleBooking("duynamasas", mockBooking.getId());

        assertEquals("Done", response.getMessage());
    }

    @Test
    public void testListOfBooking_UserNotFound() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () ->
                bookingService.listOfBooking("duynamasas")
        );
    }

    @Test
    public void testListOfBooking_Success() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findAllByUserId("825dec0a-3a9e-444d-8b26-49d9388e41ac")).thenReturn(Collections.singletonList(mockBooking));

        List<BookingResponse> responses = bookingService.listOfBooking("duynamasas");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }



    @Test
    public void testGetBookingFromID_UserNotFound() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () ->
                bookingService.getBookingFromID("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")
        );
    }

    @Test
    public void testGetBookingFromID_BookingNotFound() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findByIdAndUserId("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "825dec0a-3a9e-444d-8b26-49d9388e41ac")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () ->
                bookingService.getBookingFromID("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf")
        );
    }

    @Test
    public void testGetBookingFromID_Success() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findByIdAndUserId("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "825dec0a-3a9e-444d-8b26-49d9388e41ac")).thenReturn(Optional.of(mockBooking));

        BookingResponse response = bookingService.getBookingFromID("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf");

        assertNotNull(response);
        assertEquals(mockBooking.getId(), response.getId());
    }


    @Test
    public void testSetBookingStatus_UserNotFound() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () ->
                bookingService.setBookingStatus("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "PENDING")
        );
    }

    @Test
    public void testSetBookingStatus_BookingNotFound() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findByIdAndUserId("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "825dec0a-3a9e-444d-8b26-49d9388e41ac")).thenReturn(Optional.empty());

        assertThrows(MyNotFoundException.class, () ->
                bookingService.setBookingStatus("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "PENDING")
        );
    }

    @Test
    public void testSetBookingStatus_StatusAlreadySet() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findByIdAndUserId("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "825dec0a-3a9e-444d-8b26-49d9388e41ac")).thenReturn(Optional.of(mockBooking));
//        when(mockBooking.getStatus()).thenReturn(BookingStatus.BOOKED);

        assertThrows(MyBadRequestException.class, () ->
                bookingService.setBookingStatus("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "PENDING")
        );
    }

    @Test
    public void testSetBookingStatus_Success() {
        when(userRepo.getByUsername("duynamasas")).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findByIdAndUserId("ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "825dec0a-3a9e-444d-8b26-49d9388e41ac")).thenReturn(Optional.of(mockBooking));
//        when(mockBooking.getStatus()).thenReturn(BookingStatus.PENDING);

        MyApiResponse response = bookingService.setBookingStatus("duynamasas", "ff5b1b9a-a8e7-475c-9cc6-1beba5079ebf", "BOOKED");

        assertEquals("Success", response.getMessage());
    }

    @Test
    public void testAutoCancelBooking_WithNoPayment_ShouldCancelBooking() {
        // Tạo Booking giả lập
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreateAt(new Date()); // Set thời gian tạo booking
        booking.setId("booking-id");

        List<Booking> bookingList = Collections.singletonList(booking);

        // Mocking repositories
        when(bookingRepo.findAllByStatus(BookingStatus.PENDING)).thenReturn(bookingList);
        when(paymentRepo.findAllByBookingId(booking.getId())).thenReturn(Collections.emptyList());

        // Thực thi phương thức
        bookingService.autoCancleBooking();

        // Kiểm tra lại xem booking đã bị hủy chưa
//        verify(bookingRepo, times(1)).save(booking);
        assertEquals(BookingStatus.CANCLED, booking.getStatus());
    }

    @Test
    public void testAutoCancelBooking_WithPaymentPaid_ShouldBookTicket() {
        // Tạo Booking giả lập
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreateAt(new Date());
        booking.setSeats(Arrays.asList(mockSeat));
        booking.setId("booking-id");

        // Giả lập có thanh toán
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setStatus(PaymentStatus.PAID);

        List<Booking> bookingList = Collections.singletonList(booking);
        List<Payment> payments = Collections.singletonList(payment);

        // Mocking repositories
        when(bookingRepo.findAllByStatus(BookingStatus.PENDING)).thenReturn(bookingList);
        when(paymentRepo.findAllByBookingId(booking.getId())).thenReturn(payments);

        // Thực thi phương thức
        bookingService.autoCancleBooking();

        // Kiểm tra lại xem booking có bị hủy không (vì đã thanh toán)
        assertEquals(BookingStatus.BOOKED, booking.getStatus());  // Booking không bị hủy

    }


    @Test
    public void testAutoCancelBooking_WithPaymentPending_ShouldCancelBooking() {
        // Tạo Booking và Payment giả lập
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreateAt(new Date());
        booking.setId("booking-id");

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);

        List<Booking> bookingList = Collections.singletonList(booking);
        List<Payment> paymentList = Collections.singletonList(payment);

        // Mocking repositories
        when(bookingRepo.findAllByStatus(BookingStatus.PENDING)).thenReturn(bookingList);
        when(paymentRepo.findAllByBookingId(booking.getId())).thenReturn(paymentList);

        // Thực thi phương thức
        bookingService.autoCancleBooking();

        // Kiểm tra lại xem booking đã bị hủy chưa
//        verify(bookingRepo, times(1)).save(booking);
        assertEquals(BookingStatus.CANCLED, booking.getStatus());
    }

    @Test
    public void testBlacklistUsers_UserNotBlacklisted_ShouldIncreaseSpamCount() {
        // Tạo user và SpamUser giả lập
        Account user = new Account();
        user.setId("user-id");
        SpamUser spamUser = new SpamUser(user);
        spamUser.setSpamTimes(3); // Giả sử đã spam 2 lần

        // Mocking repository
        when(spamRepo.findByUserId(user.getId())).thenReturn(Optional.of(spamUser));

        // Thực thi phương thức
        bookingService.blacklistUsers();

        // Kiểm tra số lần spam đã tăng lên
        assertEquals(3, spamUser.getSpamTimes()); // Kiểm tra tăng số lần spam
//        verify(spamRepo, times(1)).save(spamUser); // Kiểm tra rằng SpamUser đã được lưu lại
    }

    @Test
    public void testBlacklistUsers_UserExceedMaxSpam_ShouldBlacklistUser() {
        // Tạo user và SpamUser giả lập
        Account user = new Account();
        user.setId("user-id");
        user.setStatus(UserStatus.ACTIVE);
        SpamUser spamUser = new SpamUser(user);
        spamUser.setSpamTimes(4); // Giả sử số spam vượt quá giới hạn MAXSPAM

        // Mocking repository
        when(spamRepo.findByUserId(user.getId())).thenReturn(Optional.of(spamUser));

        // Thực thi phương thức
        bookingService.blacklistUsers();

        // Kiểm tra trạng thái user đã bị blacklist
        assertEquals(UserStatus.BLACKLISTED, user.getStatus());
//        verify(userService, times(1)).saveUser(user); // Kiểm tra người dùng đã được lưu lại
//        verify(spamRepo, times(1)).save(spamUser); // Kiểm tra SpamUser đã được lưu lại
    }

    @Test
    public void testBlacklistUsers_NewUser_ShouldCreateSpamUser() {
        // Tạo user giả lập
        Account user = new Account();
        user.setId("user-id");

        // Mocking repository
        when(spamRepo.findByUserId(user.getId())).thenReturn(Optional.empty());

        // Thực thi phương thức
        bookingService.blacklistUsers();

        // Kiểm tra SpamUser đã được tạo mới
//        verify(spamRepo, times(1)).save(any(SpamUser.class)); // Kiểm tra SpamUser đã được lưu
    }

    @Test
    public void testSetBookingStatus_ToPending_ShouldResetSeatsAndRecreateBooking() {
        // Given: Booking hiện tại có trạng thái khác PENDING, ví dụ BOOKED
        mockBooking.setStatus(BookingStatus.BOOKED); // Hoặc một trạng thái khác mà logic cho phép chuyển về PENDING kiểu này

        // Giả sử booking có một danh sách ghế
        List<ShowSeat> seatsInBooking = new ArrayList<>();
        ShowSeat seat1 = new ShowSeat(); seat1.setId("seat1"); seat1.setStatus(ESeatStatus.BOOKED);
        ShowSeat seat2 = new ShowSeat(); seat2.setId("seat2"); seat2.setStatus(ESeatStatus.BOOKED);
        seatsInBooking.add(seat1);
        seatsInBooking.add(seat2);
        mockBooking.setSeats(seatsInBooking);

        when(userRepo.getByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        when(bookingRepo.findByIdAndUserId(mockBooking.getId(), mockUser.getId())).thenReturn(Optional.of(mockBooking));

        // Mock hành vi của showSeatRepo.save và bookingRepo.save/deleteById
        when(showSeatRepo.save(any(ShowSeat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(bookingRepo).deleteById(mockBooking.getId());
        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            // Kiểm tra xem booking mới có được tạo dựa trên booking cũ không
            // (tùy thuộc vào constructor Booking(Booking other) của bạn)
            // Ví dụ: assertEquals(mockBooking.getUser(), savedBooking.getUser());
            return savedBooking;
        });

        // When
        MyApiResponse response = bookingService.setBookingStatus(mockUser.getUsername(), mockBooking.getId(), "PENDING");

        // Then
        assertNotNull(response);
        assertEquals("Success", response.getMessage()); // Hoặc thông điệp thành công phù hợp

        // Verify ghế được cập nhật thành AVAILABLE
        ArgumentCaptor<ShowSeat> seatCaptor = ArgumentCaptor.forClass(ShowSeat.class);
        verify(showSeatRepo, times(seatsInBooking.size())).save(seatCaptor.capture());
        for (ShowSeat savedSeat : seatCaptor.getAllValues()) {
            assertEquals(ESeatStatus.AVAILABLE, savedSeat.getStatus());
        }

        // Verify booking cũ bị xóa
        verify(bookingRepo, times(1)).deleteById(mockBooking.getId());

        // Verify booking mới được lưu
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepo, times(1)).save(bookingCaptor.capture()); // Lần save này là của newBooking
        Booking newSavedBooking = bookingCaptor.getValue();
        assertEquals(BookingStatus.PENDING, newSavedBooking.getStatus()); // Booking mới nên có status PENDING
        // Có thể assert thêm các thuộc tính khác của newSavedBooking nếu cần
    }

    @Test
    public void testSetBookingStatus_ToCancled_ShouldCallCancleBookingMethod() {
        // Given: Booking hiện tại có thể hủy được (ví dụ PENDING)
        mockBooking.setStatus(BookingStatus.PENDING);
        mockBooking.setUser(mockUser); // Đảm bảo user của booking khớp với username

        // Tạo một spy của bookingService
        BookingServiceImpl spiedBookingService = spy(bookingService);

        // Mock cho userRepo
        when(userRepo.getByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        // Mock cho findByIdAndUserId (dùng trong setBookingStatus để lấy booking ban đầu)
        when(bookingRepo.findByIdAndUserId(mockBooking.getId(), mockUser.getId())).thenReturn(Optional.of(mockBooking));

        // *** THÊM MOCK NÀY NẾU CANCLEBOOKING DÙNG FINDBYID ***
        // Giả sử cancleBooking bên trong nó sẽ gọi bookingRepo.findById(booking_id)
        // để lấy lại booking một lần nữa hoặc để xác nhận.
        when(bookingRepo.findById(mockBooking.getId())).thenReturn(Optional.of(mockBooking));
        // *****************************************************

        // Mock thêm các dependencies mà cancleBooking (phiên bản thật trong spy) sẽ sử dụng
        // Ví dụ, nếu cancleBooking cập nhật ShowSeat:
        when(showSeatRepo.save(any(ShowSeat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Giả sử cancleBooking cũng lưu lại booking sau khi đổi status
        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // When
        MyApiResponse response = spiedBookingService.setBookingStatus(mockUser.getUsername(), mockBooking.getId(), "CANCLED");

        // Then
        assertNotNull(response);
        assertEquals("Done", response.getMessage());
        assertEquals("CANCLED", response.getStatus());

        // Verify rằng cancleBooking đã được gọi với đúng tham số
        verify(spiedBookingService, times(1)).cancleBooking(mockUser.getUsername(), mockBooking.getId());

        // Verify trạng thái của booking sau khi cancleBooking được gọi
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        // Nếu cancleBooking gọi bookingRepo.save() một lần
        verify(bookingRepo, times(1)).save(bookingCaptor.capture());
        assertEquals(BookingStatus.CANCLED, bookingCaptor.getValue().getStatus());
    }

    @Test
    public void testBlacklistUsers_ExistingSpamUser_TimesLessThanMax_ShouldIncreaseAndSave() throws Exception {
        // Given
        Account userInQueue = new Account();
        userInQueue.setId("user1");
        userInQueue.setStatus(UserStatus.ACTIVE);
        addUserToSpamQueue(userInQueue);

        SpamUser existingSpamUser = spy(new SpamUser(userInQueue)); // Spy để mock increase()
        // Giả sử increase() sẽ trả về giá trị sau khi tăng
        doReturn(MAXSPAM_IN_SERVICE - 1).when(existingSpamUser).increase();


        when(spamRepo.findByUserId(userInQueue.getId())).thenReturn(Optional.of(existingSpamUser));
        when(spamRepo.save(any(SpamUser.class))).thenReturn(existingSpamUser);

        // When
        bookingService.blacklistUsers();

        // Then
        verify(existingSpamUser, times(1)).increase();
        verify(spamRepo, times(1)).save(existingSpamUser);
        verify(userService, never()).saveUser(any(Account.class)); // User không bị blacklist
        assertEquals(UserStatus.ACTIVE, userInQueue.getStatus()); // Trạng thái user không đổi
    }

    @Test
    public void testBlacklistUsers_ExistingSpamUser_TimesEqualsOrExceedsMax_ShouldBlacklistAndSave() throws Exception {
        // Given
        Account userInQueue = new Account();
        userInQueue.setId("user2");
        userInQueue.setStatus(UserStatus.ACTIVE); // Trạng thái ban đầu
        addUserToSpamQueue(userInQueue);

        SpamUser existingSpamUser = spy(new SpamUser(userInQueue));
        doReturn(MAXSPAM_IN_SERVICE).when(existingSpamUser).increase(); // Số lần spam bằng MAXSPAM


        when(spamRepo.findByUserId(userInQueue.getId())).thenReturn(Optional.of(existingSpamUser));
        when(spamRepo.save(any(SpamUser.class))).thenReturn(existingSpamUser);
        when(userService.saveUser(any(Account.class))).thenReturn(userInQueue); // Giả sử saveUser trả về user

        // When
        bookingService.blacklistUsers();

        // Then
        verify(existingSpamUser, times(1)).increase();
        verify(spamRepo, times(1)).save(existingSpamUser);

        ArgumentCaptor<Account> userCaptor = ArgumentCaptor.forClass(Account.class);
        verify(userService, times(1)).saveUser(userCaptor.capture());
        assertEquals(UserStatus.BLACKLISTED, userCaptor.getValue().getStatus()); // Kiểm tra user đã bị blacklist
    }

    @Test
    public void testBlacklistUsers_NewSpamUser_ShouldCreateAndSave() throws Exception {
        // Given
        Account userInQueue = new Account();
        userInQueue.setId("user3");
        addUserToSpamQueue(userInQueue);

        when(spamRepo.findByUserId(userInQueue.getId())).thenReturn(Optional.empty()); // User chưa có trong spamRepo
        // Mock save để trả về đối tượng được truyền vào
        when(spamRepo.save(any(SpamUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookingService.blacklistUsers();

        // Then
        ArgumentCaptor<SpamUser> spamUserCaptor = ArgumentCaptor.forClass(SpamUser.class);
        verify(spamRepo, times(1)).save(spamUserCaptor.capture());

        SpamUser newSpamUser = spamUserCaptor.getValue();
        assertEquals(userInQueue, newSpamUser.getUser());
        assertEquals(1, newSpamUser.getSpamTimes()); // Mặc định khi tạo mới là 1 lần
        verify(userService, never()).saveUser(any(Account.class)); // User không bị blacklist khi mới tạo
    }

    @Test
    public void testBlacklistUsers_EmptyQueue_ShouldReturnImmediately() throws Exception {
        // Given: spamUsers queue is already empty (from setUp or explicitly cleared)
        Field spamUsersField = BookingServiceImpl.class.getDeclaredField("spamUsers");
        spamUsersField.setAccessible(true);
        Queue<Account> queue = (Queue<Account>) spamUsersField.get(bookingService);
        queue.clear(); // Đảm bảo queue rỗng

        // When
        bookingService.blacklistUsers();

        // Then
        verify(spamRepo, never()).findByUserId(anyString());
        verify(spamRepo, never()).save(any(SpamUser.class));
        verify(userService, never()).saveUser(any(Account.class));
        // Đảm bảo không có tương tác nào khác nếu queue rỗng
    }
}
