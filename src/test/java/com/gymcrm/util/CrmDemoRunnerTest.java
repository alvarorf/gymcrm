package com.gymcrm.util;

import com.gymcrm.core.util.CrmDemoRunner;
import com.gymcrm.dto.*;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.mapper.*;
import com.gymcrm.model.*;
import com.gymcrm.service.interfaces.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrmDemoRunnerTest {

    @Mock private GymFacade gymFacade;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;

    @Mock private TraineeMapper traineeMapper;
    @Mock private TrainerMapper trainerMapper;

    @InjectMocks
    private CrmDemoRunner runner;

    @Test
    @DisplayName("1. DEMO RUNNER: Should call createProfile and selectProfile through the Facade.")
    void runDemo_shouldExecuteWorkflow() {
        // --- ARRANGE ---
        Trainee mockTraineeEntity = Trainee.builder().username("john.doe").build();
        Trainer mockTrainerEntity = Trainer.builder().firstName("Michael").build();

        // Create real DTOs to avoid stubbing the mappers, or stub them:
        RegistrationResponse mockRegResponse = RegistrationResponse.builder().username("john.doe").build();
        TrainerProfileResponse mockTrainerResponse = TrainerProfileResponse.builder().firstName("Michael").build();

        // Stub Facade
        when(gymFacade.getTraineeService()).thenReturn(traineeService);
        when(gymFacade.getTrainerService()).thenReturn(trainerService);

        // Stub Services
        when(traineeService.createProfile(any(TraineeRegistrationRequest.class)))
                .thenReturn(mockRegResponse);

        // This now works because mockTrainerResponse is a real object, not null
        when(trainerService.selectTrainerProfile(101L))
                .thenReturn(Optional.of(mockTrainerResponse));

        // --- ACT ---
        runner.run(null);

        // --- ASSERT ---
        verify(traineeService).createProfile(any(TraineeRegistrationRequest.class));
        verify(trainerService).selectTrainerProfile(101L);
    }

}