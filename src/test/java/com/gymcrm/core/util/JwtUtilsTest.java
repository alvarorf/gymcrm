package com.gymcrm.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Utilities Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String TEST_USERNAME = "gym_user";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // ARRANGE: Set properties manually since we aren't using @SpringBootTest
        ReflectionTestUtils.setField(jwtUtils, "secretKey", "test_secret_key_for_unit_tests_12345");
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", 3600000L);
        ReflectionTestUtils.setField(jwtUtils, "issuer", "gym-crm");
        jwtUtils.init();
    }

    @Test
    @DisplayName("GENERATE TOKEN: Should create a non-null JWT string")
    void generateToken_Success() {
        // ARRANGE - Handled in @BeforeEach

        // ACT
        String token = jwtUtils.generateToken(TEST_USERNAME);

        // ASSERT
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts (header, payload, signature)");
    }

    @Test
    @DisplayName("EXTRACT USERNAME: Should return the correct subject from token")
    void extractUsername_Success() {
        // ARRANGE
        String token = jwtUtils.generateToken(TEST_USERNAME);

        // ACT
        String extracted = jwtUtils.extractUsername(token);

        // ASSERT
        assertEquals(TEST_USERNAME, extracted);
    }

    @Test
    @DisplayName("VALIDATE TOKEN: Should return true for a valid token and matching user")
    void validateToken_Valid() {
        // ARRANGE
        String token = jwtUtils.generateToken(TEST_USERNAME);
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());

        // ACT
        boolean isValid = jwtUtils.validateToken(token, userDetails);

        // ASSERT
        assertTrue(isValid);
    }

    @Test
    @DisplayName("VALIDATE TOKEN: Should return false if usernames do not match")
    void validateToken_WrongUser() {
        // ARRANGE
        String token = jwtUtils.generateToken(TEST_USERNAME);
        UserDetails wrongUser = new User("other_user", "password", Collections.emptyList());

        // ACT
        boolean isValid = jwtUtils.validateToken(token, wrongUser);

        // ASSERT
        assertFalse(isValid);
    }

    @Test
    @DisplayName("EXPIRATION: Should identify an expired token logic correctly")
    void isTokenExpired_Logic() {
        // ARRANGE: Create a token with negative expiration (already expired)
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", -1000L);
        String expiredToken = jwtUtils.generateToken(TEST_USERNAME);
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());

        // ACT & ASSERT
        // Note: The library usually throws a JWTVerificationException on expired tokens
        assertThrows(Exception.class, () -> jwtUtils.validateToken(expiredToken, userDetails));
    }
}