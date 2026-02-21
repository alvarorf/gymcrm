package com.gymcrm.controller;

import com.gymcrm.core.config.SecurityConfig;
import com.gymcrm.core.exception.AuthControllerExceptionHandler;
import com.gymcrm.core.security.JwtAuthenticationFilter;
import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.service.interfaces.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthControllerExceptionHandler.class})
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setup() throws ServletException, IOException {
        // Tell the mock filter to continue the chain,
        // otherwise the request never reaches the controller
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("LOGIN: Should return 200 OK on successful authentication")
    void login_Success() throws Exception {
        when(authService.authenticate(anyString(), anyString())).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/login")
                        .param("username", "admin")
                        .param("password", "admin")
                        .with(csrf()))
                .andExpect(status().isOk())
                // Check content string exactly
                .andExpect(content().string(Nomenclature.MSG.LOGIN_SUCCESS));
    }

    @Test
    @DisplayName("LOGIN: Should return 401 Unauthorized on bad credentials")
    void login_InvalidCredentials() throws Exception {
        // ARRANGE: Service must throw to trigger ExceptionHandler
        when(authService.authenticate(anyString(), anyString()))
                .thenThrow(new BadCredentialsException(Nomenclature.MSG.INVALID_CREDENTIALS));

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .param("username", "wrong")
                        .param("password", "wrong")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_CREDENTIALS_INVALID));
    }

    @Test
    @DisplayName("LOGIN: Should return 400 Bad Request when parameters are blank")
    void login_BlankParameters() throws Exception {
        // No need to mock service here, controller throws before calling it
        mockMvc.perform(post("/api/auth/login")
                        .param("username", "")
                        .param("password", "")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("CHANGE PASSWORD: Should return 409 Conflict when passwords are the same")
    void changePassword_SamePassword() throws Exception {
        // ARRANGE
        doThrow(new IllegalArgumentException(Nomenclature.ERR.DETAIL_SAME_PASSWORD))
                .when(authService).changePassword(anyString(), anyString(), anyString());

        String json = "{\"username\":\"u\",\"oldPassword\":\"p\",\"newPassword\":\"p\"}";

        // ACT & ASSERT
        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_SAME_PASSWORD));
    }
}
