package com.gymcrm.core.security;

import com.gymcrm.core.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JWT Authentication Filter Unit Tests")
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtFilter;
    private JwtUtils jwtUtils;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        jwtUtils = mock(JwtUtils.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        jwtFilter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        // Ensure context is clean before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("NO HEADER: Should continue filter chain without setting authentication")
    void doFilterInternal_NoHeader() throws Exception {
        // ARRANGE
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        // ACT
        jwtFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should be null");
    }

    @Test
    @DisplayName("INVALID PREFIX: Should ignore headers that don't start with Bearer")
    void doFilterInternal_InvalidPrefix() throws Exception {
        // ARRANGE
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // ACT
        jwtFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("VALID TOKEN: Should set SecurityContext for valid JWT")
    void doFilterInternal_ValidToken() throws Exception {
        // ARRANGE
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        String jwt = "valid.token.here";
        String username = "gym_user";
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtils.validateToken(jwt, userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        // ACT
        jwtFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should be set");
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("INVALID TOKEN: Should not set SecurityContext if token validation fails")
    void doFilterInternal_InvalidToken() throws Exception {
        // ARRANGE
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        String jwt = "invalid.token";
        String username = "gym_user";
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtils.validateToken(jwt, userDetails)).thenReturn(false); // Validation fails

        // ACT
        jwtFilter.doFilterInternal(request, response, filterChain);

        // ASSERT
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should remain null");
        verify(filterChain).doFilter(request, response);
    }
}
