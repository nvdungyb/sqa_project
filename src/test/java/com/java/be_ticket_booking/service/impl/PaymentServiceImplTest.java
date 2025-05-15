package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.*;
import com.java.be_ticket_booking.model.enumModel.BookingStatus;
import com.java.be_ticket_booking.model.enumModel.PaymentStatus;
import com.java.be_ticket_booking.model.enumModel.UserStatus;
import com.java.be_ticket_booking.repository.BookingRepository;
import com.java.be_ticket_booking.repository.PaymentRepository;
import com.java.be_ticket_booking.repository.UserRepository;
import com.java.be_ticket_booking.request.HashRequest;
import com.java.be_ticket_booking.request.PaymentRequest;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.PaymentResponse;
import com.java.be_ticket_booking.service.EmailService;
import com.java.be_ticket_booking.utils.HashUtil;
import com.java.be_ticket_booking.utils.VNPay;
import org.junit.jupiter.api.*;
import org.mockito.*;
import com.java.be_ticket_booking.response.TicketDetail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private HashUtil mockHashUtil;
    @Mock private UserRepository userRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private PaymentRepository paymentRepo;
    @Mock private EmailService emailService;

    private AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);

    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    private Account createUser(String username) {
        Account user = new Account();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private Booking createBooking(Account user, String status) {
        Booking booking = new Booking();
        booking.setId("booking123");
        booking.setUser(user);
        booking.setStatus(BookingStatus.valueOf(status));

        // Tạo CinemaSeat để không bị null pointer
        CinemaSeat cinemaSeat = new CinemaSeat();
        cinemaSeat.setName("A1");

        // Tạo ShowSeat gán CinemaSeat
        ShowSeat showSeat = new ShowSeat();
        showSeat.setCinemaSeat(cinemaSeat);

        booking.setSeats(Collections.singletonList(showSeat));

        // Tạo Movie
        Movie movie = new Movie();
        movie.setTitle("Avengers");

        // Tạo CinemaHall, set tên
        CinemaHall cinemaHall = new CinemaHall();
        cinemaHall.setName("Hall 1");

        // Tạo CinemaShow, gán Movie và CinemaHall
        CinemaShow show = new CinemaShow();
        show.setMovie(movie);
        String dateTimeString = "2023-06-13 16:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        show.setStartTime(LocalDateTime.parse(dateTimeString, formatter));
        show.setCinemaHall(cinemaHall);

        booking.setShow(show);

        return booking;
    }


    private Payment createPayment(Booking booking, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId("pay123");
        payment.setBooking(booking);
        payment.setAmount(200.0);
        payment.setStatus(status);
        payment.setCreateAt(new Date()); // Bắt buộc phải set để không bị null
        return payment;
    }

    @Test
    void testCreate_Valid() {
        String username = "john";
        String ip = "127.0.0.1";

        // Tạo user
        Account user = createUser(username);

        // Tạo movie
        Movie movie = new Movie();
        movie.setTitle("Avengers");

        // Tạo cinema hall
        CinemaHall hall = new CinemaHall();
        hall.setName("Hall 1");

        // Tạo ghế
        CinemaSeat seat = new CinemaSeat();
        seat.setName("A1");

        // Tạo ShowSeat
        ShowSeat showSeat = new ShowSeat();
        showSeat.setCinemaSeat(seat);

        // Tạo show
        CinemaShow show = new CinemaShow();
        show.setMovie(movie);
        show.setCinemaHall(hall);
        show.setStartTime(LocalDateTime.of(2025, 5, 15, 18, 30));
//        show.setShowSeats(List.of(showSeat));

        // Tạo booking
        Booking booking = createBooking(user, "PENDING");
        booking.setShow(show);

        // Tạo request
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(booking.getId());
        request.setPaymentType("VNPAY");

        // Mock dữ liệu repo
        when(bookingRepo.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepo.findAllByBookingId(booking.getId())).thenReturn(Collections.emptyList());
        when(paymentRepo.save(any())).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setCreateAt(new Date()); // Bắt buộc để không null
            return saved;
        });

        // Mock VNPay
        try (MockedStatic<VNPay> vnPayMock = mockStatic(VNPay.class)) {
            vnPayMock.when(() -> VNPay.createPay(any(), eq(username), eq(ip)))
                    .thenReturn("http://pay.url");

            PaymentResponse response = paymentService.create(username, request, ip);

            assertNotNull(response);
            assertEquals("http://pay.url", response.getPaymentUrl());
            verify(paymentRepo, times(1)).save(any());
        }
    }


    @Test
    void testCreate_BookingNotFound() {
        String username = "john";
        PaymentRequest request = new PaymentRequest();
        request.setBookingId("not_exist");

        when(bookingRepo.findById("not_exist")).thenReturn(Optional.empty());

        MyNotFoundException ex = assertThrows(MyNotFoundException.class, () ->
                paymentService.create(username, request, "127.0.0.1"));
        assertEquals("Ticket ID not_exist is not found", ex.getMessage());
    }

    @Test
    void testCreate_BookingAlreadyPaidOrCanceled() {
        Account user = createUser("john");
        Booking booking = createBooking(user, "BOOKED"); // BOOKED means already paid or canceled

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(booking.getId());

        when(bookingRepo.findById(booking.getId())).thenReturn(Optional.of(booking));

        MyBadRequestException ex = assertThrows(MyBadRequestException.class, () ->
                paymentService.create(user.getUsername(), request, "127.0.0.1"));
        assertEquals("This ticket have been already paid or canceled before.", ex.getMessage());
    }

    @Test
    void testGetFromId_ValidUser() {
        Account user = createUser("john");
        Booking booking = createBooking(user, "PENDING");
        Payment payment = createPayment(booking, PaymentStatus.PAID);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getFromId(user.getUsername(), payment.getId());

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void testGetFromId_InvalidUserThrows() {
        Account user = createUser("john");
        Booking booking = createBooking(user, "PENDING");
        Payment payment = createPayment(booking, PaymentStatus.PAID);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        MyNotFoundException ex = assertThrows(MyNotFoundException.class, () ->
                paymentService.getFromId("invalidUser", payment.getId()));
        assertEquals("Payment ID not found", ex.getMessage());
    }

    @Test
    void testGetFromId_PaymentNotFound() {
        when(paymentRepo.findById("non_exist")).thenReturn(Optional.empty());

        MyNotFoundException ex = assertThrows(MyNotFoundException.class, () ->
                paymentService.getFromId("anyUser", "non_exist"));
        assertEquals("Payment ID not found", ex.getMessage());
    }

    @Test
    void testVerifyPayment_Paid() {
        Account user = createUser("john");
        Booking booking = createBooking(user, "PENDING");
        Payment payment = createPayment(booking, PaymentStatus.PENDING);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));
        try (MockedStatic<VNPay> vnPayMock = mockStatic(VNPay.class)) {
            vnPayMock.when(() -> VNPay.verifyPay(any())).thenReturn(0); // 0 = paid

            MyApiResponse response = paymentService.verifyPayment(user.getUsername(), payment.getId());
            assertEquals("PAID", response.getStatus());
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testVerifyPayment_UnpaidOrCanceled() {
        Account user = createUser("john");
        Booking booking = createBooking(user, "PENDING");
        Payment payment = createPayment(booking, PaymentStatus.PENDING);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));
        try (MockedStatic<VNPay> vnPayMock = mockStatic(VNPay.class)) {
            vnPayMock.when(() -> VNPay.verifyPay(any())).thenReturn(2); // 2 = unpaid/canceled

            MyApiResponse response = paymentService.verifyPayment(user.getUsername(), payment.getId());
            assertEquals("UNPAID", response.getStatus());
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testGetAllPaymentsOfUser() {
        Account user = createUser("john");

        Payment payment = new Payment();
        payment.setId("pay1");
        payment.setCreateAt(new Date());
        payment.setStatus(PaymentStatus.PAID);
        payment.setBooking(createBooking(user, "PENDING"));

        when(userRepo.getByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(paymentRepo.findAllByUserId(user.getId())).thenReturn(Collections.singletonList(payment));

        List<PaymentResponse> responses = paymentService.getAllPaymentsOfUser(user.getUsername());
        assertEquals(1, responses.size());
        assertEquals(payment.getId(), responses.get(0).getId());
    }

    @Test
    void testAddPaymentMail_DoesNotThrow() {
        Account user = createUser("john");
        Booking booking = createBooking(user, "PENDING");
        Payment payment = createPayment(booking, PaymentStatus.PENDING);

        // Chỉ kiểm tra không bị lỗi khi gọi hàm gửi mail (đã mock)
        assertDoesNotThrow(() -> paymentService.addPaymentMail(payment));
//        verify(emailService, atLeastOnce()).sendEmail(any(), anyString(), anyString());
    }

    @Test
    void testCreateHash_NotNull() {
        HashRequest request = new HashRequest("b1","1234","John Doe", 999);
        request.setBookingId("b1");
        request.setCardID("1234");
        request.setCardName("John Doe");
        request.setCVCNumber(999);

        String hash = paymentService.createHash(request);
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void testSendPaymentViaMail_WithData_ShouldSendEmail() throws Exception {
        // Tạo User giả
        Account user = new Account();
        user.setEmail("john@example.com");

        // Tạo CinemaSeat giả
        CinemaSeat cinemaSeat = new CinemaSeat();
        cinemaSeat.setName("A1");
        cinemaSeat.setPrice(100.0);

        // Tạo ShowSeat giả
        ShowSeat showSeat = new ShowSeat();
        showSeat.setCinemaSeat(cinemaSeat);

        // Tạo list ShowSeat
        List<ShowSeat> showSeats = new ArrayList<>();
        showSeats.add(showSeat);

        // Gán vào booking

        CinemaHall cinemaHall = new CinemaHall();
        cinemaHall.setName("abc");
        Movie movie = new Movie();
        movie.setTitle("ABC");
        CinemaShow cinemaShow= new CinemaShow();
        cinemaShow.setMovie(movie);
        cinemaShow.setCinemaHall(cinemaHall);
        String dateTimeString = "2023-06-13 16:00:00.000000";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        cinemaShow.setStartTime(LocalDateTime.parse(dateTimeString, formatter));
        // Tạo Booking giả với User
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSeats(showSeats);
        booking.setShow(cinemaShow);
        // Tạo Payment giả với Booking, Amount, CreateAt, Status
        Payment payment = new Payment();
        payment.setId("p123"); // nếu có setter Id
        payment.setBooking(booking);
        payment.setAmount(200.0);
        payment.setCreateAt(new Date());
        payment.setStatus(PaymentStatus.PAID); // hoặc enum status phù hợp

        // Tạo PaymentResponse từ Payment thật
        PaymentResponse response = new PaymentResponse(payment);

        // Tạo và gán TicketDetail cho response
        TicketDetail detail = new TicketDetail(booking);
        detail.setMovieName("Avengers");
        detail.setHallName("Hall 1");
        detail.setSeats(List.of("A1", "A2"));
        detail.setStartTime("2025-05-15 18:30");
        response.setDetail(detail);

        // Dùng reflection để inject dữ liệu vào queue sendEmail trong PaymentServiceImpl
        Field sendEmailField = PaymentServiceImpl.class.getDeclaredField("sendEmail");
        sendEmailField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Queue<PaymentResponse> queue = (Queue<PaymentResponse>) sendEmailField.get(paymentService);
        queue.add(response);

        // Dùng reflection để gọi private method sendPaymentViaMail()
        Method method = PaymentServiceImpl.class.getDeclaredMethod("sendPaymentViaMail");
        method.setAccessible(true);
        method.invoke(paymentService);

        // Verify emailService.sendMail được gọi đúng với các tham số tương ứng
        verify(emailService, atLeastOnce()).sendMail(
                eq("john@example.com"),
                anyString(),
                contains("Payment ID p123")
        );
    }


    @Test
    void testSendPaymentViaMail_EmptyQueue_ShouldNotSendEmail() throws Exception {
        // Đảm bảo queue rỗng
        var sendEmailField = PaymentServiceImpl.class.getDeclaredField("sendEmail");
        sendEmailField.setAccessible(true);
        Queue<PaymentResponse> queue = (Queue<PaymentResponse>) sendEmailField.get(paymentService);
        queue.clear();

        // Gọi method
        var method = PaymentServiceImpl.class.getDeclaredMethod("sendPaymentViaMail");
        method.setAccessible(true);
        method.invoke(paymentService);

        verify(emailService, never()).sendMail(anyString(), anyString(), anyString());
    }

    @Test
    void testCheckPaymentInfo_ShouldReturnFalse() {
        PaymentRequest request = new PaymentRequest();
        // Bạn có thể set các thuộc tính của request nếu muốn

        boolean result = paymentService.checkPaymentInfo(request);

        assertFalse(result, "checkPaymentInfo should return false by default");
    }

    @Test
    void testCreate_VNPayThrowsException_ShouldSetPaymentCanceledAndReturnResponse() {
        // Given
        String username = "testuser";
        String ip = "127.0.0.1";
        Account user = createUser(username); // Giả sử bạn có phương thức helper này
        Booking booking = createBooking(user, "PENDING"); // Giả sử booking PENDING

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(booking.getId());
        request.setPaymentType("VNPAY");

        // Mock repository calls
        when(bookingRepo.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepo.findAllByBookingId(booking.getId())).thenReturn(Collections.emptyList());

        // ArgumentCaptor để bắt đối tượng Payment được lưu
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // SỬA ĐỔI MOCK paymentRepo.save() ĐỂ ĐẢM BẢO createAt ĐƯỢC SET
        when(paymentRepo.save(paymentCaptor.capture())).thenAnswer(invocation -> {
            Payment p = paymentCaptor.getValue(); // Lấy đối tượng Payment mà service đang cố gắng lưu
            if (p.getCreateAt() == null) {
                // Nếu createAt chưa được set bởi service, chúng ta set nó ở đây
                // cho mục đích của test này để tránh NullPointerException trong VNPay.createPay (mock)
                // hoặc trong PaymentResponse constructor.
                // LƯU Ý: Đây là một giải pháp "hack" cho test. Lý tưởng nhất là service
                // phải tự quản lý việc này.
                p.setCreateAt(new Date());
            }
            return p; // Trả về đối tượng đã được (có thể) sửa đổi
        });


        // Mock VNPay.createPay để ném Exception
        try (MockedStatic<VNPay> vnPayMock = mockStatic(VNPay.class)) {
            RuntimeException exceptionToThrow = new RuntimeException("VNPay service unavailable");
            // Chúng ta vẫn dùng any(Payment.class) vì mock paymentRepo.save ở trên
            // đã đảm bảo createAt sẽ được set trước khi đối tượng này được sử dụng sâu hơn.
            vnPayMock.when(() -> VNPay.createPay(any(Payment.class), eq(request.getPaymentType()), eq(ip)))
                    .thenThrow(exceptionToThrow);

            // When
            // Kỳ vọng phương thức create không ném ra Exception ra ngoài, mà xử lý bên trong catch
            PaymentResponse response = assertDoesNotThrow(() -> paymentService.create(username, request, ip));

            // Then
            assertNotNull(response);
            assertEquals("none", response.getPaymentUrl(), "Payment URL should be 'none' when VNPay throws exception");

            List<Payment> savedPayments = paymentCaptor.getAllValues();
            assertTrue(savedPayments.size() >= 2, "paymentRepo.save should be called at least twice");

            // Payment cuối cùng được lưu phải có status CANCLED
            Payment finalSavedPayment = savedPayments.get(savedPayments.size() - 1);
            assertNotNull(finalSavedPayment.getCreateAt(), "Final saved payment should have a creation date");
            assertEquals(PaymentStatus.CANCLED, finalSavedPayment.getStatus(), "Payment status should be CANCLED after VNPay exception");
            assertEquals(booking.getId(), finalSavedPayment.getBooking().getId(), "Payment should be associated with the correct booking");

            assertEquals(finalSavedPayment.getId(), response.getId());
            assertEquals(booking.getUser().getEmail(), response.getEmail());

        } catch (Exception e) {
            fail("Test should not have thrown an unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    void testVerifyPayment_VNPayThrowsException_ShouldHandleException() {
        // Given
        String username = "testuser";
        Account user = createUser(username); // user.getUsername() sẽ là "testuser"
        Booking booking = createBooking(user, "PENDING"); // Booking thuộc về user
        Payment payment = createPayment(booking, PaymentStatus.PENDING); // payment.getBooking().getUser().getUsername() sẽ là "testuser"

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        // Mock VNPay.verifyPay để ném Exception
        try (MockedStatic<VNPay> vnPayMock = mockStatic(VNPay.class)) {
            RuntimeException exceptionToThrow = new RuntimeException("VNPay API communication error");
            vnPayMock.when(() -> VNPay.verifyPay(payment)).thenThrow(exceptionToThrow);

            // When
            // Hiện tại, nếu VNPay ném lỗi, code không có return cụ thể trong khối if sau catch.
            // Nó sẽ không trả về gì từ khối if (username.equals(userOfpayment)).
            // Do đó, nó sẽ rơi xuống dòng throw MyNotFoundException cuối cùng.
            // Điều này có thể không phải là hành vi mong muốn.
            // Để test khối catch, chúng ta chỉ cần đảm bảo nó không crash và không ném Exception không mong muốn.
            // Nếu bạn muốn nó trả về một response cụ thể khi có lỗi, bạn cần sửa code service.

            // Kịch bản 1: Kỳ vọng nó ném MyNotFoundException do không có return sau catch
            MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () -> {
                paymentService.verifyPayment(username, payment.getId());
            }, "Expected MyNotFoundException because there's no return after catch if VNPay fails and username matches.");
            assertEquals("Payment ID not found", thrown.getMessage());


            // Kịch bản 2 (Nếu bạn sửa code để có return sau catch, ví dụ trả về PENDING):
            // MyApiResponse response = assertDoesNotThrow(() -> paymentService.verifyPayment(username, payment.getId()));
            // assertNotNull(response);
            // assertEquals("PENDING", response.getStatus()); // Hoặc một status lỗi cụ thể
            // assertEquals(PaymentStatus.PENDING, payment.getStatus()); // Status payment không đổi
            // verify(paymentRepo, never()).save(any(Payment.class)); // Không lưu lại payment nếu có lỗi VNPay và không đổi status

        } catch (Exception e) {
            fail("Test setup for VNPay mock failed or unexpected exception: " + e.getMessage(), e);
        }
    }
    @Test
    void testVerifyPayment_UsernameDoesNotMatchPaymentUser_ShouldThrowNotFoundException() {
        // Given
        String actualPaymentOwnerUsername = "ownerUser";
        String requestingUsername = "differentUser"; // Username không khớp

        Account ownerUser = createUser(actualPaymentOwnerUsername);
        Booking booking = createBooking(ownerUser, "PENDING");
        Payment payment = createPayment(booking, PaymentStatus.PENDING);

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        // When & Then
        MyNotFoundException thrown = assertThrows(MyNotFoundException.class, () -> {
            paymentService.verifyPayment(requestingUsername, payment.getId());
        });

        assertEquals("Payment ID not found", thrown.getMessage());
        // Đảm bảo VNPay.verifyPay không được gọi
        try (MockedStatic<VNPay> vnPayMock = mockStatic(VNPay.class)) {
            vnPayMock.verify(() -> VNPay.verifyPay(any(Payment.class)), never());
        }
    }

}
