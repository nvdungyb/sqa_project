package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.MyBadRequestException;
import com.java.be_ticket_booking.exception.MyNotFoundException;
import com.java.be_ticket_booking.model.Account;
import com.java.be_ticket_booking.model.Role;
import com.java.be_ticket_booking.model.enumModel.ERole;
import com.java.be_ticket_booking.model.enumModel.UserStatus;
import com.java.be_ticket_booking.repository.RoleRepository;
import com.java.be_ticket_booking.repository.UserRepository;
import com.java.be_ticket_booking.response.AccountSummaryResponse;
import com.java.be_ticket_booking.response.MyApiResponse;
import com.java.be_ticket_booking.response.EmailResponse;
import com.java.be_ticket_booking.security.InputValidationFilter;
import com.java.be_ticket_booking.service.EmailService;
import org.json.JSONObject;
import org.springframework.security.crypto.password.PasswordEncoder; // Added import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository UserREPO;

    @Mock
    private RoleRepository RoleREPO;

    @Mock
    private EmailService emailSER;

    @Mock
    private InputValidationFilter inputValidationSER;

    @Mock
    private PasswordEncoder passwordEncoder; // Properly declared

    @InjectMocks
    private UserServiceImpl userService;
    @InjectMocks
    private UserServiceImpl spyService; // Declared here

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailService emailService;


    private Account mockAccount;

    UserServiceImplTest() {
    }

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockAccount.setUsername("testuser");
        mockAccount.setEmail("test@example.com");
        mockAccount.setPassword("password123");
        mockAccount.setRoles(new HashSet<>());
        mockAccount.setStatus(UserStatus.ACTIVE);
        mockAccount.setCreate_at(new Date());
    }

    @Test
    void saveUser_shouldReturnSavedUser() {
        when(userRepository.save(mockAccount)).thenReturn(mockAccount);
        Account saved = userService.saveUser(mockAccount);
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void saveRole_shouldReturnSavedRole() {
        Role role = new Role();
        when(roleRepository.save(role)).thenReturn(role);
        Role saved = userService.saveRole(role);
        assertEquals(role, saved);
    }

    @Test
    void getUserByUsername_shouldReturnUser() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        AccountSummaryResponse res = userService.getUserByUsername("testuser");
        assertEquals("testuser", res.getUsername());
    }

    @Test
    void addRoleToUser_shouldAddRole() {
        Role role = new Role();
        role.setRole(ERole.ROLE_ADMIN);
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(roleRepository.findByName("ADMIN")).thenReturn(role);
        userService.addRoleToUser("testuser", ERole.ROLE_ADMIN);
        verify(userRepository).save(mockAccount);
    }

    @Test
    void getUsers_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(mockAccount));
        List<AccountSummaryResponse> res = userService.getUsers();
        assertEquals(1, res.size());
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        assertNotNull(userService.loadUserByUsername("testuser"));
    }

    @Test
    void loadUserByUsername_shouldThrowException() {
        when(userRepository.getByUsername("notexist")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("notexist"));
    }

    @Test
    void UsernameIsExisted_shouldThrowBadRequest() {
        assertThrows(MyBadRequestException.class, () -> userService.UsernameIsExisted("in valid"));
    }

    @Test
    void UsernameIsExisted_shouldReturnTrueIfExists() {
        when(userRepository.existsByUsername("validUser")).thenReturn(true);
        assertTrue(userService.UsernameIsExisted("validUser"));
    }

    @Test
    void UsernameIsExisted_shouldReturnFalseIfNotExists() {
        when(userRepository.existsByUsername("validUser")).thenReturn(false);
        assertFalse(userService.UsernameIsExisted("validUser"));
    }

    @Test
    void EmailIsExisted_shouldThrowBadRequest() {
        assertThrows(MyBadRequestException.class, () -> userService.EmailIsExisted("invalid"));
    }

    @Test
    void EmailIsExisted_shouldReturnTrueIfExists() {
        when(userRepository.existsByEmail("email@example.com")).thenReturn(true);
        assertTrue(userService.EmailIsExisted("email@example.com"));
    }

    @Test
    void EmailIsExisted_shouldReturnFalseIfNotExists() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);
        assertFalse(userService.EmailIsExisted("nonexistent@example.com"));
    }

    @Test
    void PasswordIsGood_shouldThrowBadRequest() {
        assertThrows(MyBadRequestException.class, () -> userService.PasswordIsGood("123"));
    }

    @Test
    void PasswordIsGood_shouldReturnTrueIfValid() {
        String validPassword = "Valid123!";
        assertTrue(userService.PasswordIsGood(validPassword));
    }

    @Test
    void getRawUserByUsername_shouldReturnUser() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        assertEquals(mockAccount, userService.getRawUserByUsername("testuser"));
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        when(userRepository.getByEmail("test@example.com")).thenReturn(Optional.of(mockAccount));
        AccountSummaryResponse res = userService.getUserByEmail("test@example.com");
        assertEquals("testuser", res.getUsername());
    }

    @Test
    void searchByName_shouldReturnList() {
        when(userRepository.findByUsernameContaining("test")).thenReturn(List.of(mockAccount));
        assertEquals(1, userService.searchByName("test").size());
    }

    @Test
    void deleteUserByUsername_shouldDelete() {
        userService.deteleUserByUsername("testuser");
        verify(userRepository).deleteByUsername("testuser");
    }

    @Test
    void getRoleFromUser_shouldReturnRoles() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        assertEquals(0, userService.getRoleFromUser("testuser").size());
    }

    @Test
    void userHaveRole_shouldCheckRole() {
        Role role = new Role();
        role.setRole(ERole.ROLE_ADMIN);
        mockAccount.setRoles(Set.of(role));
        assertTrue(userService.userHaveRole(mockAccount, ERole.ROLE_ADMIN));
    }

    @Test
    void userHaveRole_shouldReturnFalseIfRoleNotPresent() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        assertFalse(userService.userHaveRole("testuser", ERole.ROLE_ADMIN));
    }

    @Test
    void removeRoleUser_shouldRemoveRole() {
        Role role = new Role();
        role.setRole(ERole.ROLE_ADMIN);
        mockAccount.setRoles(Set.of(role));
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(roleRepository.findByName("ADMIN")).thenReturn(role);
        userService.removeRoleUser("testuser", ERole.ROLE_ADMIN);
        verify(userRepository).save(any());
    }

    @Test
    void removeRoleUser_shouldHandleNonExistentRole() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(roleRepository.findByName("ADMIN")).thenReturn(null);
        userService.removeRoleUser("testuser", ERole.ROLE_ADMIN);
        verify(userRepository).save(mockAccount);
    }

    @Test
    void removeRoleUser_shouldHandleEmptyRoles() {
        mockAccount.setRoles(new HashSet<>());
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        userService.removeRoleUser("testuser", ERole.ROLE_ADMIN);
        verify(userRepository).save(mockAccount);
    }

    @Test
    void getURIforgetPassword_shouldSendEmail() throws Exception {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        MyApiResponse res = userService.getURIforgetPassword("testuser");
        assertEquals("Please check your email", res.getMessage());
    }

    @Test
    void getURIforgetPassword_shouldHandleEmailSendFailure() throws Exception {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        doThrow(new RuntimeException("Email send failed")).when(emailService).sendMail(anyString(), anyString(), anyString());
        MyApiResponse res = userService.getURIforgetPassword("testuser");
        assertEquals("Please check your email", res.getMessage());
    }

    @Test
    void checkRecoveryCode_shouldThrowIfInvalid() {
        doReturn(null).when(spyService).checkToken("invalidCode");
        assertThrows(MyNotFoundException.class, () -> spyService.checkReocveryCode("invalidCode"));
    }

    @Test
    void checkRecoveryCode_shouldThrowIfTokenExpired() {
        JSONObject json = new JSONObject();
        json.put("username", "testuser");
        json.put("expired", "2020-01-01");
        doReturn(json).when(spyService).checkToken("expiredCode");
        assertThrows(MyNotFoundException.class, () -> spyService.checkReocveryCode("expiredCode"));
    }

    @Test
    void checkRecoveryCode_shouldThrowIfJsonInvalid() {
        doReturn(new JSONObject()).when(spyService).checkToken("invalidJsonCode");
        assertThrows(MyNotFoundException.class, () -> spyService.checkReocveryCode("invalidJsonCode"));
    }

    @Test
    void checkRecoveryCode_shouldReturnValidToken() {
        JSONObject json = new JSONObject();
        json.put("username", "testuser");
        json.put("expired", "2099-12-31");
        doReturn(json).when(spyService).checkToken("validCode");
        MyApiResponse res = spyService.checkReocveryCode("validCode");
        assertEquals("Token is valid", res.getMessage());
    }

    @Test
    void setNewPassword_shouldThrowIfInvalidToken() {
        doReturn(null).when(spyService).checkToken("invalid");
        assertThrows(MyNotFoundException.class, () -> spyService.setNewPassword("invalid", "abc123"));
    }

    @Test
    void setNewPassword_shouldUpdatePasswordIfValid() {
        JSONObject json = new JSONObject();
        json.put("username", "testuser");
        json.put("expired", "2099-12-31");
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(inputValidationSER.checkInput("Valid123!")).thenReturn(true);
        when(passwordEncoder.encode("Valid123!")).thenReturn("encodedPassword");
        doReturn(json).when(spyService).checkToken("validCode");
        MyApiResponse res = spyService.setNewPassword("validCode", "Valid123!");
        assertEquals("Set new password", res.getMessage());
        verify(userRepository).save(mockAccount);
    }

    @Test
    void setNewPassword_illegalPassword_throwsBadRequest() {
        JSONObject json = new JSONObject();
        json.put("username", "testuser");
        json.put("expired", "2099-12-31");
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(inputValidationSER.checkInput("bad$password")).thenReturn(false);
        doReturn(json).when(spyService).checkToken("validCode");
        MyBadRequestException ex = assertThrows(MyBadRequestException.class, () ->
                spyService.setNewPassword("validCode", "bad$password"));
        assertEquals("Contain illegal character", ex.getMessage());
    }

    @Test
    void countAccFromIP_shouldReturnCount() {
        when(userRepository.countByIp("127.0.0.1")).thenReturn(3);
        assertEquals(3, userService.countAccFromIP("127.0.0.1"));
    }

    @Test
    void sendRestCodeMail_sendsAllEmails() throws Exception {
        Field mailQueueField = UserServiceImpl.class.getDeclaredField("mailQueue");
        mailQueueField.setAccessible(true);
        Queue<EmailResponse> mailQueue = (Queue<EmailResponse>) mailQueueField.get(userService);

        EmailResponse email1 = new EmailResponse("a@a.com", "subject1", "content1");
        EmailResponse email2 = new EmailResponse("b@b.com", "subject2", "content2");

        mailQueue.offer(email1);
        mailQueue.offer(email2);

        userService.sendRestCodeMail();

        verify(emailService, times(1)).sendMail(email1.getMail(), email1.getSubject(), email1.getContent());
        verify(emailService, times(1)).sendMail(email2.getMail(), email2.getSubject(), email2.getContent());
    }

    @Test
    void sendRestCodeMail_shouldSkipIfQueueEmpty() {
        userService.sendRestCodeMail();
        verify(emailService, never()).sendMail(anyString(), anyString(), anyString());
    }
    @Test
    void getUserByName_shouldReturnUser() {
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        AccountSummaryResponse res = userService.getUserByName("testuser");
        assertEquals("testuser", res.getUsername());
    }
    @Test
    void userHaveRole_shouldReturnTrueIfRolePresent() {
        Role role = new Role();
        role.setRole(ERole.ROLE_ADMIN);
        mockAccount.setRoles(Set.of(role));
        when(userRepository.getByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        assertTrue(userService.userHaveRole("testuser", ERole.ROLE_ADMIN));
    }
    @Test
    void checkRecoveryCode_shouldThrowIfJsonParsingFails() {
        // Simulate Base64util.decode5Times returning invalid JSON
        doThrow(new RuntimeException("JSON parsing error")).when(spyService).checkToken("invalidJsonCode");
        assertThrows(MyNotFoundException.class, () -> spyService.checkReocveryCode("invalidJsonCode"));
    }
    @Test
    void getRawUserByUsername_shouldThrowIfNotFound() {
        when(userRepository.getByUsername("notexist")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getRawUserByUsername("notexist"));
    }
    @Test
    void getUserByEmail_shouldThrowIfNotFound() {
        when(userRepository.getByEmail("notexist@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByEmail("notexist@example.com"));
    }
    @Test
    void getUserByName_shouldThrowIfNotFound() {
        when(userRepository.getByUsername("notexist")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByName("notexist"));
    }
    @Test
    void searchByName_shouldReturnEmptyListIfNoMatches() {
        when(userRepository.findByUsernameContaining("nonexistent")).thenReturn(Collections.emptyList());
        assertTrue(userService.searchByName("nonexistent").isEmpty());
    }
    @Test
    void getRoleFromUser_shouldThrowIfUserNotFound() {
        when(userRepository.getByUsername("notexist")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getRoleFromUser("notexist"));
    }
    @Test
    void getURIforgetPassword_shouldThrowIfUserNotFound() {
        when(userRepository.getByUsername("notexist")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getURIforgetPassword("notexist"));
    }
    @Test
    void saveUser_shouldThrowIfNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.saveUser(null));
    }
    @Test
    void getUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        assertTrue(userService.getUsers().isEmpty());
    }
}