package com.gymcrm.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Auth Configuration Unit Tests")
class AuthConfigTest {

    private AuthConfig authConfig;

    @BeforeEach
    void setUp() {
        authConfig = new AuthConfig();
    }

    @Test
    @DisplayName("PASSWORD ENCODER: Should provide a BCryptPasswordEncoder instance")
    void passwordEncoder_ReturnsBCryptInstance() {
        // ARRANGE - No complex arrangement needed for this bean

        // ACT
        PasswordEncoder encoder = authConfig.passwordEncoder();

        // ASSERT
        assertNotNull(encoder, "PasswordEncoder bean should not be null");
        assertTrue(encoder instanceof BCryptPasswordEncoder, "Should specifically return BCrypt implementation");
    }

    @Test
    @DisplayName("AUTHENTICATION MANAGER: Should retrieve manager from configuration")
    void authenticationManager_ReturnsManagerFromConfig() throws Exception {
        // ARRANGE
        // We mock the AuthenticationConfiguration that Spring usually provides
        AuthenticationConfiguration mockConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(mockConfig.getAuthenticationManager()).thenReturn(mockManager);

        // ACT
        AuthenticationManager result = authConfig.authenticationManager(mockConfig);

        // ASSERT
        assertNotNull(result, "AuthenticationManager should be successfully retrieved");
        assertEquals(mockManager, result, "The returned manager should be the one provided by the configuration");
        verify(mockConfig, times(1)).getAuthenticationManager();
    }
}
