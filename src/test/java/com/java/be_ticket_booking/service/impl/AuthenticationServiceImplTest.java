package com.java.be_ticket_booking.service.impl;
import com.java.be_ticket_booking.response.EmailResponse;
import com.java.be_ticket_booking.exception.MyUnauthorizedException;
import com.java.be_ticket_booking.exception.InvalidInputException;
import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyAccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.java.be_ticket_booking.model.Account;
import com.java.be_ticket_booking.model.AccountTemp;
import com.java.be_ticket_booking.model.JWTToken;
import com.java.be_ticket_booking.model.enumModel.ERole;
import com.java.be_ticket_booking.model.enumModel.UserStatus;
import com.java.be_ticket_booking.repository.UserTempRepository;
import com.java.be_ticket_booking.request.LoginRequest;
import com.java.be_ticket_booking.request.SignUpRequest;
import com.java.be_ticket_booking.response.AuthenJWTokenResponse;
import com.java.be_ticket_booking.response.AuthenticationResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.security.InputValidationFilter;
import com.java.be_ticket_booking.service.EmailService;
import com.java.be_ticket_booking.service.JWTokenService;
import com.java.be_ticket_booking.service.JwtService;
import com.java.be_ticket_booking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @InjectMocks
    private AuthenticationServiceImpl authService;
    @Mock
    private UserService userService;

    @Mock
    private InputValidationFilter inputValidationFilter;

    @Mock
    private UserTempRepository userTempRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private JWTokenService jwtTokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;
    private Account account;
    private AccountTemp accountTemp;
    private JWTToken jwtToken;

    @BeforeEach
    void setUp() {
        // Initialize SignUpRequest with setters
        signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("Password123!");
        signUpRequest.setEmail("new@example.com");
        signUpRequest.setFullname("New User");
        signUpRequest.setPhone("1234567890");
        signUpRequest.setAddress("123 Street");

        // Initialize LoginRequest with setters
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");

        // Initialize Account
        account = new Account();
        account.setId(UUID.randomUUID().toString());
        account.setUsername("testuser");
        account.setPassword("encodedPassword");
        account.setEmail("test@example.com");
        account.setFullname("Test User");
        account.setPhone("1234567890");
        account.setAddress("123 Street");
        account.setStatus(UserStatus.ACTIVE);

        // Initialize AccountTemp
        accountTemp = new AccountTemp();
        accountTemp.setUsername("newuser");
        accountTemp.setPassword("encodedPassword");
        accountTemp.setEmail("new@example.com");
        accountTemp.setFullname("New User");
        accountTemp.setPhone("1234567890");
        accountTemp.setAddress("123 Street");
        accountTemp.setCode("validCode");
        accountTemp.setIp("127.0.0.1");

        // Initialize JWTToken
        jwtToken = new JWTToken();
        jwtToken.setUser(account);
        jwtToken.setAccessToken("encryptedAccessToken");
        jwtToken.setRefreshToken("encryptedRefreshToken");

        // Set default redirectURL and base_verified_url
        setField(authenticationService, "redirectURL", "http://localhost");
        setField(authenticationService, "base_verified_url", "http://localhost/verify/");
    }

    // Helper method to set private field
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    @Test
    void TC_AUTH_001_signup_success() {
        when(inputValidationFilter.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().toLowerCase());
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(userService.UsernameIsExisted("newuser")).thenReturn(false);
        when(userService.EmailIsExisted("new@example.com")).thenReturn(false);
        when(userTempRepository.existsByUsername("newuser")).thenReturn(false);
        when(userTempRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.PasswordIsGood("Password123!")).thenReturn(true);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userTempRepository.save(any(AccountTemp.class))).thenReturn(accountTemp);

        MyApiResponse response = authenticationService.signup(signUpRequest, "127.0.0.1");

        assertEquals("Please, go to your email to verify your account", response.getMessage());
        verify(userTempRepository, times(1)).save(any(AccountTemp.class));
        verify(emailService, never()).sendMail(anyString(), anyString(), anyString());
    }

    @Test
    void TC_AUTH_002_signup_existingUsername_throwsBadRequest() {
        when(inputValidationFilter.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().toLowerCase());
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(userService.UsernameIsExisted("newuser")).thenReturn(true);

        MyBadRequestException ex = assertThrows(MyBadRequestException.class,
                () -> authenticationService.signup(signUpRequest, "127.0.0.1"));

        assertEquals("Username is existed or Waiting for verifying email", ex.getMessage());
        verify(userTempRepository, never()).save(any());
    }

    @Test
    void TC_AUTH_003_signup_existingEmail_throwsBadRequest() {
        when(inputValidationFilter.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().toLowerCase());
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(userService.UsernameIsExisted("newuser")).thenReturn(false);
        when(userService.EmailIsExisted("new@example.com")).thenReturn(true);

        MyBadRequestException ex = assertThrows(MyBadRequestException.class,
                () -> authenticationService.signup(signUpRequest, "127.0.0.1"));

        assertEquals("Email is existed or Waiting for verifying email", ex.getMessage());
        verify(userTempRepository, never()).save(any());
    }

    @Test
    void TC_AUTH_004_signup_badPassword_throwsBadRequest() {
        signUpRequest.setPassword("weakpass");
        when(inputValidationFilter.sanitizeInput(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().toLowerCase());
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(userService.UsernameIsExisted("newuser")).thenReturn(false);
        when(userService.EmailIsExisted("new@example.com")).thenReturn(false);
        when(userTempRepository.existsByUsername("newuser")).thenReturn(false);
        when(userTempRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userService.PasswordIsGood("weakpass")).thenReturn(false);

        MyBadRequestException ex = assertThrows(MyBadRequestException.class,
                () -> authenticationService.signup(signUpRequest, "127.0.0.1"));

        assertEquals("Password is so bad", ex.getMessage());
        verify(userTempRepository, never()).save(any());
    }

    @Test
    void TC_AUTH_005_signup_invalidInput_throwsInvalidInput() {
        signUpRequest.setUsername("user<script>");
        when(inputValidationFilter.sanitizeInput(anyString())).thenReturn("user<script>");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(false);

        MyBadRequestException ex = assertThrows(MyBadRequestException.class,
                () -> authenticationService.signup(signUpRequest, "127.0.0.1"));

        assertEquals("Data containt illegal character", ex.getMessage());
        verify(userTempRepository, never()).save(any());
    }

    @Test
    void TC_AUTH_006_login_validCredentials_newToken_success() {
        // Khởi tạo loginRequest
        LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
        Account account = new Account();
        account.setUsername("testuser");
        account.setEmail("test@example.com"); // Đảm bảo account có email

        // Mock với tham số chính xác
        when(inputValidationFilter.sanitizeInput(loginRequest.getUsername())).thenReturn("testuser");
        when(inputValidationFilter.checkInput(loginRequest.getUsername())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        when(userService.getRawUserByUsername(loginRequest.getUsername())).thenReturn(account);
        // Chỉ mock userHaveRole nếu login sử dụng nó
        when(userService.userHaveRole(account, ERole.ROLE_ADMIN)).thenReturn(false); // Xóa nếu không dùng
        when(jwtTokenService.getData(account)).thenReturn(null); // Xóa nếu không dùng
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(jwtService.generateToken(any(), eq(account))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(), eq(account))).thenReturn("encryptedRefreshToken"); // Khớp với assert
        when(jwtTokenService.saveInfo(eq(account), eq("accessToken"), eq("encryptedRefreshToken"))).thenReturn(jwtToken);

        AuthenticationResponse result = authenticationService.login(loginRequest, request, false);

        assertEquals("accessToken", result.getAccessToken());
        assertEquals("encryptedRefreshToken", result.getRefreshToken());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(jwtTokenService, times(1)).saveInfo(eq(account), eq("accessToken"), eq("encryptedRefreshToken"));
    }

    @Test
    void TC_AUTH_007_login_invalidCredentials_throwsException() {
        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        MyAccessDeniedException exception = assertThrows(MyAccessDeniedException.class,
                () -> authenticationService.login(loginRequest, request, false));
        assertEquals("Username or password is wrong", exception.getMessage());
        verify(jwtTokenService, never()).saveInfo(any(), anyString(), anyString());
    }

    @Test
    void TC_AUTH_008_login_adminLogin_noAdminRole_throwsException() {
        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "Password123!"));
        when(userService.getRawUserByUsername("testuser")).thenReturn(account);
        when(userService.userHaveRole(account, ERole.ROLE_ADMIN)).thenReturn(false);

        Exception exception = assertThrows(Exception.class,
                () -> authenticationService.login(loginRequest, request, true));
        System.out.println("Exception thrown: " + exception.getClass().getName() + " - " + exception.getMessage());
    }

    @Test
    void TC_AUTH_009_login_validCredentials_expiredAccessToken_success() {
        AuthenJWTokenResponse tokenData = new AuthenJWTokenResponse();
        tokenData.setData(jwtToken);
        tokenData.setAccessDecrypt("accessToken");
        tokenData.setRefreshDecrypt("refreshToken");

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "Password123!"));
        when(userService.getRawUserByUsername("testuser")).thenReturn(account);
        when(userService.userHaveRole(account, ERole.ROLE_ADMIN)).thenReturn(false);
        when(jwtTokenService.getData(account)).thenReturn(tokenData);
        when(jwtService.isValidToken("refreshToken", account, false)).thenReturn(true);
        when(jwtService.isValidToken("accessToken", account, true)).thenReturn(false);
        when(jwtService.generateTokenFromRefreshToken("refreshToken")).thenReturn("newAccessToken");
        when(jwtTokenService.setAccessToken(eq(jwtToken), eq("newAccessToken"))).thenReturn("encryptedNewAccessToken");

        AuthenticationResponse result = authenticationService.login(loginRequest, request, false);

        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("encryptedRefreshToken", result.getRefreshToken());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(jwtTokenService, times(1)).setAccessToken(any(), anyString());
    }

    @Test
    void TC_AUTH_010_login_validCredentials_validTokens_success() {
        AuthenJWTokenResponse tokenData = new AuthenJWTokenResponse();
        tokenData.setData(jwtToken);
        tokenData.setAccessDecrypt("accessToken");
        tokenData.setRefreshDecrypt("refreshToken");

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "Password123!"));
        when(userService.getRawUserByUsername("testuser")).thenReturn(account);
        when(userService.userHaveRole(account, ERole.ROLE_ADMIN)).thenReturn(false);
        when(jwtTokenService.getData(account)).thenReturn(tokenData);
        when(jwtService.isValidToken("refreshToken", account, false)).thenReturn(true);
        when(jwtService.isValidToken("accessToken", account, true)).thenReturn(true);

        AuthenticationResponse result = authenticationService.login(loginRequest, request, false);

        assertEquals("accessToken", result.getAccessToken());
        assertEquals("encryptedRefreshToken", result.getRefreshToken());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(jwtTokenService, never()).saveInfo(any(), anyString(), anyString());
        verify(jwtTokenService, never()).setAccessToken(any(), anyString());
    }

    @Test
    void TC_AUTH_011_login_invalidInput_throwsException() {
        loginRequest.setUsername("user<script>");
        when(inputValidationFilter.sanitizeInput("user<script>")).thenReturn("user<script>");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(false);

        MyBadRequestException exception = assertThrows(MyBadRequestException.class,
                () -> authenticationService.login(loginRequest, request, false));
        assertEquals("Data containt illegal character", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void TC_AUTH_012_verifyCode_validCode_redirectsToLogin() {
        when(userTempRepository.findByCode("validCode")).thenReturn(Optional.of(accountTemp));
        when(userService.saveUser(any(Account.class))).thenReturn(account);
        doNothing().when(userService).addRoleToUser("newuser", ERole.ROLE_USER);
        doNothing().when(userTempRepository).deleteByUsername("newuser");

        authenticationService.veriyCode("validCode", response);

        verify(response).setHeader("Location", "http://localhost/login");
        verify(response).setStatus(302);
        verify(userService, times(1)).saveUser(any(Account.class));
        verify(userService, times(1)).addRoleToUser("newuser", ERole.ROLE_USER);
        verify(userTempRepository, times(1)).deleteByUsername("newuser");
    }

    @Test
    void TC_AUTH_013_verifyCode_invalidCode_redirectsTo404() {
        when(userTempRepository.findByCode("invalidCode")).thenReturn(Optional.empty());

        authenticationService.veriyCode("invalidCode", response);

        verify(response).setHeader("Location", "http://localhost/404");
        verify(response).setStatus(302);
        verify(userService, never()).saveUser(any());
        verify(userTempRepository, never()).deleteByUsername(anyString());
    }

    @Test
    void TC_AUTH_014_refreshAccessToken_validTokens_success() {
        AuthenJWTokenResponse tokenData = new AuthenJWTokenResponse();
        tokenData.setData(jwtToken);
        tokenData.setAccessDecrypt("accessToken");
        tokenData.setRefreshDecrypt("validRefreshToken");

        when(jwtTokenService.getFromRefreshToken("validRefreshToken")).thenReturn(tokenData);
        when(jwtService.isValidToken("accessToken", account, true)).thenReturn(true);

        AuthenticationResponse result = authenticationService.refreshAccessToken("validRefreshToken", request);

        assertEquals("accessToken", result.getAccessToken());
        assertEquals("encryptedRefreshToken", result.getRefreshToken());
        assertEquals("", result.getUsername());
        assertEquals("", result.getEmail());
        verify(jwtTokenService, never()).setAccessToken(any(), anyString());
    }

    @Test
    void TC_AUTH_015_refreshAccessToken_expiredAccessToken_success() {
        AuthenJWTokenResponse tokenData = new AuthenJWTokenResponse();
        tokenData.setData(jwtToken);
        tokenData.setAccessDecrypt("accessToken");
        tokenData.setRefreshDecrypt("validRefreshToken");

        when(jwtTokenService.getFromRefreshToken("validRefreshToken")).thenReturn(tokenData);
        when(jwtService.isValidToken("accessToken", account, true)).thenReturn(false);
        when(jwtService.isValidToken("validRefreshToken", account, false)).thenReturn(true);
        when(jwtService.generateTokenFromRefreshToken("validRefreshToken")).thenReturn("newAccessToken");
        when(jwtTokenService.setAccessToken(eq(jwtToken), eq("newAccessToken"))).thenReturn("encryptedNewAccessToken");

        AuthenticationResponse result = authenticationService.refreshAccessToken("validRefreshToken", request);

        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("encryptedRefreshToken", result.getRefreshToken());
        assertEquals("", result.getUsername());
        assertEquals("", result.getEmail());
        verify(jwtTokenService, times(1)).setAccessToken(eq(jwtToken), eq("newAccessToken"));
    }
    @Test
    void TC_AUTH_016_refreshAccessToken_invalidRefreshToken_throwsException() {
        // Khi token không tồn tại trong db/cache thì trả về null
        when(jwtTokenService.getFromRefreshToken("invalidToken")).thenReturn(null);

        MyUnauthorizedException exception = assertThrows(MyUnauthorizedException.class,
                () -> authenticationService.refreshAccessToken("invalidToken", request));

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void TC_AUTH_017_refreshAccessToken_expiredRefreshToken_throwsException() {
        // Chuẩn bị dữ liệu token trả về
        AuthenJWTokenResponse tokenData = new AuthenJWTokenResponse();
        tokenData.setData(jwtToken);
        tokenData.setAccessDecrypt("expiredAccessToken");
        tokenData.setRefreshDecrypt("expiredRefreshToken");

        // Mock trả về tokenData khi gọi với refresh token
        when(jwtTokenService.getFromRefreshToken("expiredRefreshToken")).thenReturn(tokenData);

        // Giả định lấy account từ jwtToken
        Account userAccount = jwtToken.getUser();

        // Mock kiểm tra token expired => false
        when(jwtService.isValidToken("expiredAccessToken", userAccount, true)).thenReturn(false);
        when(jwtService.isValidToken("expiredRefreshToken", userAccount, false)).thenReturn(false);

        // Thực thi và bắt exception
        Exception exception = assertThrows(MyUnauthorizedException.class, () -> {
            authenticationService.refreshAccessToken("expiredRefreshToken", request);
        });

        // Kiểm tra message của exception
        assertEquals("Token expired", exception.getMessage());
    }
    @Test
    void TC_AUTH_018_login_userNotFound_throwsException() {
        String sanitizedUsername = "testuser";

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn(sanitizedUsername);
        when(inputValidationFilter.checkInput(sanitizedUsername)).thenReturn(true);

        // Giả lập authentication thành công
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(sanitizedUsername, "Password123!"));

        // Nhưng userService trả về null (user không tồn tại)
        when(userService.getRawUserByUsername(sanitizedUsername)).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.login(loginRequest, request, false));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void TC_AUTH_019_login_inputInvalid_throwsException() {
        String sanitizedUsername = "testuser";

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn(sanitizedUsername);
        when(inputValidationFilter.checkInput(sanitizedUsername)).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> authenticationService.login(loginRequest, request, false));

        assertEquals("Invalid input", exception.getMessage());
    }
    @Test
    void TC_AUTH_020_sendVerifyMail_sendsEmailFromQueue() throws NoSuchFieldException, IllegalAccessException {
        EmailResponse testEmail = new EmailResponse("test@example.com", "Subject", "Content");

        // Inject queue via Reflection or set it directly if accessible
        Field field = AuthenticationServiceImpl.class.getDeclaredField("mailQueue");
        field.setAccessible(true);
        Queue<EmailResponse> queue = new LinkedList<>();
        queue.add(testEmail);
        field.set(authenticationService, queue); // Inject test queue

        doNothing().when(emailService).sendMail(anyString(), anyString(), anyString());

        authenticationService.sendVerifyMail();

        verify(emailService, times(1)).sendMail("test@example.com", "Subject", "Content");
    }
    @Test
    void login_ShouldCreateTokens_WhenDataIsNull() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        Account mockAccount = new Account();
        mockAccount.setUsername("testuser");
        mockAccount.setEmail("test@example.com");

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);

        when(userService.getRawUserByUsername("testuser")).thenReturn(mockAccount);
        when(jwtTokenService.getData(mockAccount)).thenReturn(null); // trigger branch 1

        when(jwtService.generateToken(any(), eq(mockAccount))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), eq(mockAccount))).thenReturn("refresh-token");

        JWTToken mockToken = new JWTToken();
        mockToken.setRefreshToken("refresh-token");
        when(jwtTokenService.saveInfo(eq(mockAccount), anyString(), anyString())).thenReturn(mockToken);

        // Act
        AuthenticationResponse response = authService.login(request, mockRequest, false);

        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }
    @Test
    void login_ShouldUpdateTokens_WhenRefreshTokenInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        Account mockAccount = new Account();
        mockAccount.setUsername("testuser");

        JWTToken oldToken = new JWTToken();
        oldToken.setRefreshToken("old-refresh");

        AuthenJWTokenResponse data = mock(AuthenJWTokenResponse.class);
        when(data.getData()).thenReturn(oldToken);
        when(data.getAccessDecrypt()).thenReturn("access-decrypt");
        when(data.getRefreshDecrypt()).thenReturn("refresh-decrypt");

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(userService.getRawUserByUsername("testuser")).thenReturn(mockAccount);
        when(jwtTokenService.getData(mockAccount)).thenReturn(data);

        // refresh token is invalid → createTokens(..., true)
        when(jwtService.isValidToken("refresh-decrypt", mockAccount, false)).thenReturn(false);

        when(jwtService.generateToken(any(), eq(mockAccount))).thenReturn("new-access");
        when(jwtService.generateRefreshToken(any(), eq(mockAccount))).thenReturn("new-refresh");

        JWTToken newToken = new JWTToken();
        newToken.setRefreshToken("new-refresh");
        when(jwtTokenService.updateInfo(oldToken, "new-access", "new-refresh")).thenReturn(newToken);

        // Act
        AuthenticationResponse response = authService.login(request, mockRequest, false);

        // Assert
        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
    }
    @Test
    void login_ShouldGenerateNewAccessToken_WhenAccessTokenInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        Account mockAccount = new Account();
        mockAccount.setUsername("testuser");

        JWTToken oldToken = new JWTToken();
        oldToken.setRefreshToken("refresh-token");

        AuthenJWTokenResponse data = mock(AuthenJWTokenResponse.class);
        when(data.getData()).thenReturn(oldToken);
        when(data.getAccessDecrypt()).thenReturn("old-access");
        when(data.getRefreshDecrypt()).thenReturn("refresh-decrypt");

        when(inputValidationFilter.sanitizeInput("testuser")).thenReturn("testuser");
        when(inputValidationFilter.checkInput(anyString())).thenReturn(true);
        when(userService.getRawUserByUsername("testuser")).thenReturn(mockAccount);
        when(jwtTokenService.getData(mockAccount)).thenReturn(data);

        // accessToken invalid → regenerate
        when(jwtService.isValidToken("refresh-decrypt", mockAccount, false)).thenReturn(true);
        when(jwtService.isValidToken("old-access", mockAccount, true)).thenReturn(false);

        when(jwtService.generateTokenFromRefreshToken("refresh-decrypt")).thenReturn("regenerated-access");

        // Act
        AuthenticationResponse response = authService.login(request, mockRequest, false);

        // Assert
        assertEquals("regenerated-access", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

}