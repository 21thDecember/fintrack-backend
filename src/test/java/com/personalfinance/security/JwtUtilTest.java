package com.personalfinance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretKey = "0123456789012345678901234567890123456789012345678901234567890123";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        jwtUtil = new JwtUtil(objectMapper, secretKey, 3600000L); // 1 hour
    }

    @Test
    void testGenerateAndValidateToken() {
        String username = "admin";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.structure"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testTamperedToken() {
        String token = jwtUtil.generateToken("admin");
        String tampered = token + "tampered";
        assertFalse(jwtUtil.validateToken(tampered));
    }
}
