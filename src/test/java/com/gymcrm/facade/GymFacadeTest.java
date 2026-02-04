package com.gymcrm.facade;

import com.gymcrm.service.interfaces.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gym Facade unit tests")
class GymFacadeTest {

    @Mock private AuthService authService;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;

    @InjectMocks
    private GymFacade gymFacade;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("LOGIN SUCCESS: Should return true when authService returns UserDetails")
    void login_Success() {
        // ARRANGE
        String user = "john.doe";
        String pass = "secret";

        // Fix: authenticate returns UserDetails, not boolean
        UserDetails mockUser = new User(user, pass, Collections.emptyList());
        when(authService.authenticate(user, pass)).thenReturn(mockUser);

        // ACT
        boolean result = gymFacade.login(user, pass);

        // ASSERT
        assertTrue(result, "Login should return true when authentication succeeds");
        verify(authService, times(1)).authenticate(user, pass);
    }

    @Test
    @DisplayName("LOGIN FAILURE: Should return false when authService throws UsernameNotFoundException")
    void login_Failure() {
        // ARRANGE
        String user = "wrong.user";
        String pass = "wrong.pass";

        // Fix: Facade catches this specific exception to return false
        when(authService.authenticate(user, pass))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // ACT
        boolean result = gymFacade.login(user, pass);

        // ASSERT
        assertFalse(result, "Login should return false when exception is thrown");
        verify(authService).authenticate(user, pass);
    }

    @Test
    @DisplayName("SERVICE ACCESS: Should ensure all services are accessible via getters")
    void getServices_ReturnsMocks() {
        // ACT & ASSERT
        // Since we use @InjectMocks and have mocked the dependencies, these should be present
        assertNotNull(gymFacade.getTraineeService(), "TraineeService should be injected");
        assertNotNull(gymFacade.getTrainerService(), "TrainerService should be injected");
        assertNotNull(gymFacade.getTrainingService(), "TrainingService should be injected");
    }
}