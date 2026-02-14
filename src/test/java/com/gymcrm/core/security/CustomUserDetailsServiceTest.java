package com.gymcrm.core.security;

import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.mapper.UserMapper;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.repositories.TrainerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("SUCCESS: Should return UserDetails when Trainee is found")
    void loadUserByUsername_TraineeFound() {
        // ARRANGE
        String username = "alice.trainee";
        Trainee trainee = new Trainee();
        UserDetails expectedDetails = mock(UserDetails.class);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));
        when(userMapper.toUserDetails(trainee)).thenReturn(expectedDetails);

        // ACT
        UserDetails result = userDetailsService.loadUserByUsername(username);

        // ASSERT
        assertNotNull(result);
        assertEquals(expectedDetails, result);
        verify(traineeRepository).findByUsername(username);
        verifyNoInteractions(trainerRepository); // Should stop after finding Trainee
    }

    @Test
    @DisplayName("SUCCESS: Should return UserDetails from Trainer when Trainee is not found")
    void loadUserByUsername_TrainerFound() {
        // ARRANGE
        String username = "bob.trainer";
        Trainer trainer = new Trainer();
        UserDetails expectedDetails = mock(UserDetails.class);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));
        when(userMapper.toUserDetails(trainer)).thenReturn(expectedDetails);

        // ACT
        UserDetails result = userDetailsService.loadUserByUsername(username);

        // ASSERT
        assertNotNull(result);
        assertEquals(expectedDetails, result);
        verify(traineeRepository).findByUsername(username);
        verify(trainerRepository).findByUsername(username);
    }

    @Test
    @DisplayName("EXCEPTION: Should throw UsernameNotFoundException when user exists in neither repository")
    void loadUserByUsername_NotFound() {
        // ARRANGE
        String username = "ghost.user";
        String expectedMessage = Nomenclature.getNotFoundMsg(username);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername(username)
        );

        assertEquals(expectedMessage, exception.getMessage());
        verify(traineeRepository).findByUsername(username);
        verify(trainerRepository).findByUsername(username);
        verifyNoInteractions(userMapper);
    }
}