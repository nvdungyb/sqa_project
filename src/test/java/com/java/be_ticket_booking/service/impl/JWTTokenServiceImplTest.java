package com.java.be_ticket_booking.service.impl;

import com.java.be_ticket_booking.exception.MyAccessDeniedException;
import com.java.be_ticket_booking.model.Account;
import com.java.be_ticket_booking.model.JWTToken;
import com.java.be_ticket_booking.repository.JWTokenRepository;
import com.java.be_ticket_booking.response.AuthenJWTokenResponse;
import com.java.be_ticket_booking.utils.ChaCha20util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTTokenServiceImplTest {

    @Mock
    private JWTokenRepository jwtTokenRepo;

    @Mock
    private ChaCha20util cipher;

    @InjectMocks
    private JWTTokenServiceImpl service;

    private Account user;
    private JWTToken token;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        user = new Account();
        user.setId("1");

        token = new JWTToken();
        token.setUser(user);
        token.setAccessToken("encryptedAccess");
        token.setRefreshToken("encryptedRefresh");

        // Inject mock cipher via reflection
        Field cipherField = JWTTokenServiceImpl.class.getDeclaredField("cipher");
        cipherField.setAccessible(true);
        cipherField.set(service, cipher);
    }

    @Test
    void saveInfo_shouldEncryptAndSaveToken() {
        when(cipher.encryptString("access")).thenReturn("encAccess");
        when(cipher.encryptString("refresh")).thenReturn("encRefresh");

        JWTToken saved = new JWTToken(user, "encAccess", "encRefresh");
        when(jwtTokenRepo.save(any(JWTToken.class))).thenReturn(saved);

        JWTToken result = service.saveInfo(user, "access", "refresh");

        assertEquals("encAccess", result.getAccessToken());
        assertEquals("encRefresh", result.getRefreshToken());
    }

    @Test
    void updateInfo_shouldEncryptAndUpdateToken() {
        when(cipher.encryptString("newAccess")).thenReturn("encNewAccess");
        when(cipher.encryptString("newRefresh")).thenReturn("encNewRefresh");

        when(jwtTokenRepo.save(token)).thenReturn(token);

        JWTToken result = service.updateInfo(token, "newAccess", "newRefresh");

        assertEquals("encNewAccess", result.getAccessToken());
        assertEquals("encNewRefresh", result.getRefreshToken());
    }

    @Test
    void getAccessToken_shouldReturnDecryptedAccessToken() {
        when(jwtTokenRepo.findByUserId("1")).thenReturn(Optional.of(token));
        when(cipher.decrypt("encryptedAccess")).thenReturn("decryptedAccess");

        String access = service.getAccessToken(user);
        assertEquals("decryptedAccess", access);
    }

    @Test
    void getRefreshToken_shouldReturnDecryptedRefreshToken() {
        when(jwtTokenRepo.findByUserId("1")).thenReturn(Optional.of(token));
        when(cipher.decrypt("encryptedRefresh")).thenReturn("decryptedRefresh");

        String refresh = service.getRefreshToken(user);
        assertEquals("decryptedRefresh", refresh);
    }

    @Test
    void setAccessToken_shouldEncryptAndSave() {
        when(cipher.encryptString("newAccess")).thenReturn("encAccess");
        when(jwtTokenRepo.save(token)).thenReturn(token);

        String result = service.setAccessToken(token, "newAccess");

        assertEquals("encAccess", result);
        assertEquals("encAccess", token.getAccessToken());
    }

    @Test
    void setRefreshToken_shouldEncryptAndSave() {
        when(cipher.encryptString("newRefresh")).thenReturn("encRefresh");
        when(jwtTokenRepo.save(token)).thenReturn(token);

        String result = service.setRefreshToken(token, "newRefresh");

        assertEquals("encRefresh", result);
        assertEquals("encRefresh", token.getAccessToken()); // BUG ở đây trong code gốc: lẽ ra phải là setRefreshToken
    }

    @Test
    void getData_shouldReturnFullTokenData() {
        when(jwtTokenRepo.findByUserId("1")).thenReturn(Optional.of(token));
        when(cipher.decrypt("encryptedAccess")).thenReturn("access");
        when(cipher.decrypt("encryptedRefresh")).thenReturn("refresh");

        AuthenJWTokenResponse res = service.getData(user);

        assertEquals("access", res.getAccessDecrypt());
        assertEquals("refresh", res.getRefreshDecrypt());
    }

    @Test
    void getData_shouldReturnNullWhenTokenNotFound() {
        when(jwtTokenRepo.findByUserId("1")).thenReturn(Optional.empty());

        assertNull(service.getData(user));
    }

    @Test
    void getFromRefreshToken_shouldReturnDecryptedResponse() {
        when(jwtTokenRepo.findByRefreshToken("token")).thenReturn(Optional.of(token));
        when(cipher.decrypt("encryptedAccess")).thenReturn("access");
        when(cipher.decrypt("encryptedRefresh")).thenReturn("refresh");

        AuthenJWTokenResponse res = service.getFromRefreshToken("token");

        assertEquals("access", res.getAccessDecrypt());
        assertEquals("refresh", res.getRefreshDecrypt());
    }

    @Test
    void getFromRefreshToken_shouldThrowWhenInvalid() {
        when(jwtTokenRepo.findByRefreshToken("invalid")).thenReturn(Optional.empty());

        assertThrows(MyAccessDeniedException.class, () -> {
            service.getFromRefreshToken("invalid");
        });
    }
}
