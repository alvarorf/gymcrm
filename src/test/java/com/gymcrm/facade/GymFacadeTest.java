package com.gymcrm.facade;

import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Gym Facade Unit Tests")
class GymFacadeTest {

    @Mock private ApplicationContext applicationContext;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;

    @InjectMocks
    private GymFacade gymFacade;

    /*
    @BeforeEach
    void setUp() {
        // ARRANGE: We use an overloaded constructor in GymFacade to inject our mock context
        // call inside the default constructor.
        // It skips AnnotationConfigApplicationContext startup.
        gymFacade = new GymFacade(applicationContext);

        // Setup default behavior for the context mock to return our service mocks
        lenient().when(applicationContext.getBean(TraineeService.class)).thenReturn(traineeService);
        lenient().when(applicationContext.getBean(TrainerService.class)).thenReturn(trainerService);
        lenient().when(applicationContext.getBean(TrainingService.class)).thenReturn(trainingService);
    }
    */

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("LOGIN SUCCESS: Should authenticate via Trainee and set SecurityContext")
    void login_TraineeSuccess() {
        // ARRANGE
        String user = "john.doe";
        String pass = "secret";
        when(traineeService.authenticate(user, pass)).thenReturn(true);
        // trainerService does NOT need to be mocked for success because it won't be called

        // ACT
        boolean result = gymFacade.login(user, pass);

        // ASSERT
        assertTrue(result, "Login should return true for valid Trainee");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // VERIFY: Now with short-circuiting, trainerService is NEVER called
        verify(traineeService).authenticate(user, pass);
        verify(trainerService, never()).authenticate(anyString(), anyString());
    }

    @Test
    @DisplayName("LOGIN SUCCESS: Should authenticate via Trainer if Trainee check fails")
    void login_TrainerSuccess() {
        // ARRANGE
        String user = "coach.bob";
        String pass = "workout123";
        when(traineeService.authenticate(user, pass)).thenReturn(false);
        when(trainerService.authenticate(user, pass)).thenReturn(true);

        // ACT
        boolean result = gymFacade.login(user, pass);

        // ASSERT
        assertTrue(result, "Login should return true for valid Trainer");
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("LOGIN FAILURE: Should return false and empty context for invalid credentials")
    void login_Failure() {
        // ARRANGE
        String user = "wrong.user";
        String pass = "wrong.pass";
        when(traineeService.authenticate(user, pass)).thenReturn(false);
        when(trainerService.authenticate(user, pass)).thenReturn(false);

        // ACT
        boolean result = gymFacade.login(user, pass);

        // ASSERT
        assertFalse(result, "Login should return false for invalid credentials");
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext should remain empty");
    }

    @Test
    @DisplayName("SERVICE ACCESS: Should retrieve services from the mock context")
    void getServices_ReturnsMocks() {
        // ACT & ASSERT
        assertNotNull(gymFacade.getTraineeService());
        assertNotNull(gymFacade.getTrainerService());
        assertNotNull(gymFacade.getTrainingService());

        verify(applicationContext, times(1)).getBean(TraineeService.class);
    }
}