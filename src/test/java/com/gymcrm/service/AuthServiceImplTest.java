package com.gymcrm.service;

import com.gymcrm.core.util.JwtUtils;
import com.gymcrm.dao.interfaces.*;
import com.gymcrm.model.Trainee;
import com.gymcrm.core.security.CustomUserDetailsService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service unit tests")
class AuthServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private JwtUtils jwtUtils;
    @Mock private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthServiceImpl authService;


    @Test
    @DisplayName("AUTHENTICATE: Should return JWT token on valid credentials")
    void authenticate_Success() {
        // ARRANGE
        String username = "test.user";
        String password = "password123";
        String mockToken = "eyJhbGci...jwt.token";

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtUtils.generateToken(username)).thenReturn(mockToken);

        // ACT
        String result = authService.authenticate(username, password);

        // ASSERT
        assertNotNull(result);
        assertEquals(mockToken, result);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("AUTHENTICATE: Should propagate BadCredentialsException when auth fails")
    void authenticate_Failed() {
        // ARRANGE
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        // ACT & ASSERT
        Assertions.assertThrows(BadCredentialsException.class, () ->
                authService.authenticate("user", "wrong_pass"));
    }


    @Test
    @DisplayName("LOAD USER: Should delegate to CustomUserDetailsService")
    void loadUserByUsername_Delegation() {
        // ARRANGE
        String username = "trainee.joe";
        UserDetails mockDetails = User.builder()
                .username(username)
                .password("encoded_pass")
                .roles("TRAINEE")
                .build();

        // We stub the custom service, because that's what AuthServiceImpl calls
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(mockDetails);

        // ACT
        UserDetails result = authService.loadUserByUsername(username);

        // ASSERT
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(customUserDetailsService, times(1)).loadUserByUsername(username);
    }
    @Test
    @DisplayName("CHANGE PASSWORD: Should encode and update trainee password")
    void changePassword_TraineeSuccess() {
        // ARRANGE
        String username = "john.doe";
        String oldPass = "old123";
        String newPass = "new456";
        String encodedPass = "encoded_new456";
        Trainee trainee = Trainee.builder().username(username).password(oldPass).build();

        UserDetails mockDetails = User.builder().username(username).password(oldPass).roles("TRAINEE").build();
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(mockDetails, oldPass);

        // Stubbing all necessary dependencies
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode(newPass)).thenReturn(encodedPass);

        // ACT
        authService.changePassword(username, oldPass, newPass);

        // ASSERT
        assertEquals(encodedPass, trainee.getPassword());
        verify(passwordEncoder).encode(newPass);
        verify(traineeDao).save(trainee);
    }
}
