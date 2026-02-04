package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.config.SecurityConfig;
import com.gymcrm.config.logging.*;
import com.gymcrm.dto.PasswordChangeRequest;
import com.gymcrm.exception.GlobalExceptionHandler;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.util.Nomenclature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// We @Import to explicitly bring the Security and Logging configurations
@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        TransactionFilter.class,
        RestLoggingFilter.class,
        GlobalExceptionHandler.class
})
@DisplayName("Authentication controller unit tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;

    @Test
    @DisplayName("LOGIN SUCCESS: Should return 200 and success message")
    void login_Success() throws Exception {
        UserDetails mockUser = new User("test.user", "pass", Collections.emptyList());
        when(authService.authenticate("test.user", "pass")).thenReturn(mockUser);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "test.user")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Transaction-ID")) // Verifies TransactionFilter works!
                .andExpect(content().string(Nomenclature.MSG_LOGIN_SUCCESS));
    }

    @Test
    @DisplayName("LOGIN FAILURE: Invalid credentials (null user) should return 401")
    void login_InvalidCredentials() throws Exception {
        when(authService.authenticate(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "wrong")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(Nomenclature.MSG_INVALID_CREDENTIALS));
    }

    @Test
    @WithMockUser
    @DisplayName("CHANGE PASSWORD SUCCESS: Should return 200 OK")
    void changePassword_Success() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest("user", "old", "new");
        doNothing().when(authService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(put("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(Nomenclature.MSG_PASSWORD_CHANGED));
    }
}