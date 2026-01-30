package com.gymcrm.service;

import com.gymcrm.dao.interfaces.*;
import com.gymcrm.mapper.UserMapper;
import com.gymcrm.model.Trainee;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Unit Tests")
class AuthServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("AUTHENTICATE: Should return UserDetails on valid credentials")
    void authenticate_Success() {
        // ARRANGE
        String user = "test.user";
        String pass = "pass";
        UserDetails mockDetails = User.builder().username(user).password(pass).roles("TRAINEE").build();
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(mockDetails, pass);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        // ACT
        UserDetails result = authService.authenticate(user, pass);

        // ASSERT
        assertEquals(user, result.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("LOAD USER: Should load trainee if present")
    void loadUserByUsername_TraineeFound() {
        // ARRANGE
        String username = "trainee.joe";
        Trainee mockTrainee = Trainee.builder().username(username).build();
        UserDetails mockDetails = User.builder().username(username).password("p").roles("TRAINEE").build();

        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(mockTrainee));
        when(userMapper.toUserDetails(mockTrainee)).thenReturn(mockDetails);

        // ACT
        UserDetails result = authService.loadUserByUsername(username); // TODO: We get a warning: java.lang.IllegalArgumentException: password cannot be null. Handle and test for the exception

        // ASSERT
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(traineeDao).findByUsername(username);
        verify(trainerDao, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("CHANGE PASSWORD: Should update trainee password after successful auth")
    void changePassword_TraineeSuccess() {
        // ARRANGE
        String username = "john.doe";
        String oldPass = "old123";
        String newPass = "new456";
        Trainee trainee = Trainee.builder().username(username).password(oldPass).build();

        UserDetails mockDetails = User.builder().username(username).password(oldPass).roles("TRAINEE").build();
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(mockDetails, oldPass);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(traineeDao.findByUsername(username)).thenReturn(Optional.of(trainee));

        // ACT
        authService.changePassword(username, oldPass, newPass);

        // ASSERT
        assertEquals(newPass, trainee.getPassword());
        verify(traineeDao).save(trainee);
    }
}
