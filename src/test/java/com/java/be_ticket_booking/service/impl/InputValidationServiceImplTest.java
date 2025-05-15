package com.java.be_ticket_booking.service.impl;

import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidationServiceImplTest {

    private InputValidationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InputValidationServiceImpl();
    }

    @Test
    void sanitizeInput() {
        String raw = "<script>alert('xss')</script>";
        String expected = "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;";
        String actual = service.sanitizeInput(raw);
        assertEquals(expected, actual);
    }

    @Test
    void sanitizeInputWithSafeList() {
        String raw = "<div><a href='http://example.com'>link</a><script>alert(1)</script></div>";
        String expected = "<a href=\"http://example.com\" rel=\"nofollow\">link</a>";
        String actual = service.sanitizeInputWithSafeList(raw, Safelist.basic());
        assertEquals(expected, actual);
    }

    @Test
    void containsSqlInjection() {
        String sqlInjection = "' OR 1=1 --";
        // Giả sử RegexExtractor.SQL phù hợp với chuỗi này
        assertTrue(service.containsSqlInjection(sqlInjection));

        String normal = "Hello world";
        assertFalse(service.containsSqlInjection(normal));
    }

    @Test
    void containsXss() {
        String xss = "<script>alert('XSS')</script>";
        // Giả sử RegexExtractor.XSS phù hợp với chuỗi này
        assertTrue(service.containsXss(xss));

        String normal = "Safe text";
        assertFalse(service.containsXss(normal));
    }

    @Test
    void checkInput() {
        String sqlInjection = "'; DROP TABLE users; --";
        assertFalse(service.checkInput(sqlInjection));

        String safeInput = "Hello, this is a safe input.";
        assertTrue(service.checkInput(safeInput));
    }
}
