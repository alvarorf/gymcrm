package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.core.config.SecurityConfig;
import com.gymcrm.core.config.logging.*;
import com.gymcrm.dto.PasswordChangeRequest;
import com.gymcrm.core.exception.AuthControllerExceptionHandler;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.core.util.Nomenclature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        TransactionFilter.class,
        RestLoggingFilter.class,
        AuthControllerExceptionHandler.class // Using specific handler instead of Global
})
@DisplayName("Authentication controller expanded unit tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private AuthService authService;

    // --- LOGIN COVERAGE ---

    @Test
    @DisplayName("LOGIN EMPTY: Should return 400 with Nomenclature message via JSON")
    void login_EmptyCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isBadRequest()) // Now matches 400
                .andExpect(jsonPath("$.message").value(Nomenclature.MSG_AUTH_REQUIRED))
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_CREDENTIALS_INVALID));
    }

    @Test
    @DisplayName("LOGIN NOT FOUND: Should return 404 from ExceptionHandler")
    void login_UserNotFound() throws Exception {
        String errMsg = Nomenclature.getNotFoundMsg("unknown.user");
        when(authService.authenticate(anyString(), anyString()))
                .thenThrow(new UsernameNotFoundException(errMsg));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "unknown.user")
                        .param("password", "any"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_USER_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(errMsg));
    }

    @Test
    @DisplayName("LOGIN WRONG PASS: Should return 401 via BadCredentialsException")
    void login_WrongPassword() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenThrow(new BadCredentialsException(Nomenclature.MSG_INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "test.user")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_CREDENTIALS_INVALID))
                .andExpect(jsonPath("$.message").value(Nomenclature.MSG_INVALID_CREDENTIALS));
    }

    // --- CHANGE PASSWORD COVERAGE ---

    @Test
    @WithMockUser
    @DisplayName("PW CHANGE VALIDATION: Missing new password should return 400")
    void changePassword_ValidationFailure() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest("user", "old", ""); // Empty new password

        mockMvc.perform(put("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors.newPassword").value(Nomenclature.REQD.NEW_PASSWORD));
    }

    @Test
    @WithMockUser
    @DisplayName("PW CHANGE CONFLICT: Same password should return 409")
    void changePassword_SamePasswordConflict() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest("user", "same", "same");

        doThrow(new IllegalArgumentException(Nomenclature.ERR.DETAIL_SAME_PASSWORD))
                .when(authService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(put("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_SAME_PASSWORD))
                .andExpect(jsonPath("$.details").value(Nomenclature.ERR.DETAIL_SAME_PASSWORD));
    }

    // --- SUCCESS COVERAGE ---

    @Test
    @DisplayName("LOGIN SUCCESS: Should return 200 and success message")
    void login_Success() throws Exception {
        // Mock success: return a dummy UserDetails object
        UserDetails dummyUser = User.withUsername("test.user")
                .password("encodedPass")
                .roles("USER")
                .build();

        when(authService.authenticate("test.user", "correctPass"))
                .thenReturn(dummyUser);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "test.user")
                        .param("password", "correctPass"))
                .andExpect(status().isOk())
                .andExpect(content().string(Nomenclature.MSG_LOGIN_SUCCESS));

        // This confirms the controller reached the TODO line
        verify(authService, times(1)).authenticate("test.user", "correctPass");
    }

    @Test
    @WithMockUser
    @DisplayName("PW CHANGE SUCCESS: Should return 200 and change message")
    void changePassword_Success() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest("user", "old", "new");

        // void methods in Mockito do nothing by default, which is what we want for success
        doNothing().when(authService).changePassword("user", "old", "new");

        mockMvc.perform(put("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(Nomenclature.MSG_PASSWORD_CHANGED));

        // Verifies the service was called, proving the controller logic completed
        verify(authService, times(1)).changePassword("user", "old", "new");
    }
}