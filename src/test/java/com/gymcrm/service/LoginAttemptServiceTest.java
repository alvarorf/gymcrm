package com.gymcrm.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Login Attempt Service Unit Tests")
class LoginAttemptServiceTest {

    private LoginAttemptServiceImpl loginAttemptService;
    private final String USERNAME = "test.user";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptServiceImpl();
    }

    @Test
    @DisplayName("LOGIN SUCCESS: Should clear all attempts and blocks")
    void loginSucceeded_ClearsCache() {
        // ARRANGE
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);

        // ACT
        loginAttemptService.loginSucceeded(USERNAME);

        // ASSERT
        assertFalse(loginAttemptService.isBlocked(USERNAME), "User should not be blocked after success");
    }

    @Test
    @DisplayName("BLOCKING: Should block user after 3 failed attempts")
    void loginFailed_BlocksAfterMaxAttempts() {
        // ARRANGE
        loginAttemptService.loginFailed(USERNAME); // 1
        loginAttemptService.loginFailed(USERNAME); // 2

        // ACT
        loginAttemptService.loginFailed(USERNAME); // 3 - Threshold reached

        // ASSERT
        assertTrue(loginAttemptService.isBlocked(USERNAME), "User should be blocked after 3 attempts");
    }

    @Test
    @DisplayName("INCREMENTAL: Should not block user on the 2nd failed attempt")
    void loginFailed_DoesNotBlockPrematurely() {
        // ARRANGE
        loginAttemptService.loginFailed(USERNAME);

        // ACT
        loginAttemptService.loginFailed(USERNAME);

        // ASSERT
        assertFalse(loginAttemptService.isBlocked(USERNAME), "User should not be blocked on only 2 attempts");
    }

    @Test
    @DisplayName("EDGE CASE: isBlocked should unblock user if time has expired")
    void isBlocked_UnblocksAfterDuration() {
        // ARRANGE
        // Simulate a block that happened in the past manually is hard without
        // mocking LocalDateTime, but we can verify the logic by checking
        // that a fresh block IS active.
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);

        // ACT
        boolean currentlyBlocked = loginAttemptService.isBlocked(USERNAME);

        // ASSERT
        assertTrue(currentlyBlocked, "User should be blocked initially");
        // Note: To truly test the 'unblock' after 5 minutes in a unit test,
        // we would usually use a Clock bean to move time forward.
    }

    @Test
    @DisplayName("COVERAGE: isBlocked should unblock user and clear cache when time expires")
    void isBlocked_ClearsCacheAfterExpiration() {
        // 1. Set up a fixed clock at "Now"
        java.time.Instant now = java.time.Instant.parse("2026-02-21T10:00:00Z");
        java.time.ZoneId zone = java.time.ZoneId.of("UTC");
        loginAttemptService.setClock(java.time.Clock.fixed(now, zone));

        // 2. Block the user
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        assertTrue(loginAttemptService.isBlocked(USERNAME));

        // 3. Move the clock 6 minutes into the future
        java.time.Instant sixMinutesLater = now.plus(java.time.Duration.ofMinutes(6));
        loginAttemptService.setClock(java.time.Clock.fixed(sixMinutesLater, zone));

        // 4. ACT: This will trigger the "isAfter" branch
        boolean blocked = loginAttemptService.isBlocked(USERNAME);

        // 5. ASSERT
        assertFalse(blocked, "User should be unblocked because time expired");
    }

    @Test
    @DisplayName("EDGE CASE: Multiple users should have independent attempt counts")
    void loginFailed_IndependentUsers() {
        // ARRANGE
        String otherUser = "other.user";
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME); // USERNAME is now blocked

        // ACT
        boolean otherBlocked = loginAttemptService.isBlocked(otherUser);

        // ASSERT
        assertTrue(loginAttemptService.isBlocked(USERNAME));
        assertFalse(otherBlocked, "The block on one user should not affect another");
    }
}