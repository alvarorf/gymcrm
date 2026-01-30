package com.gymcrm.util;

import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TrainerProfileResponse;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.mapper.TraineeMapper;
import com.gymcrm.mapper.TrainerMapper;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
        // Setup mock entities
        Trainee mockTraineeEntity = Trainee.builder()
                .username("john.doe")
                .password("securePass123")
                .build();

        TrainingType motivationType = TrainingType.builder()
                .trainingTypeName("Motivation")
                .build();

        Trainer mockTrainerEntity = Trainer.builder()
                .firstName("Michael")
                .lastName("Scott")
                .specialization(motivationType)
                .build();

        // Use Mappers to generate expected DTO responses
        RegistrationResponse mockRegResponse = traineeMapper.toRegistrationResponse(mockTraineeEntity);
        TrainerProfileResponse mockTrainerResponse = trainerMapper.toProfileResponse(mockTrainerEntity);

        // Stub Facade and Services
        when(gymFacade.getTraineeService()).thenReturn(traineeService);
        when(gymFacade.getTrainerService()).thenReturn(trainerService);

        // Fix: Use DTO types for parameters and return values
        when(traineeService.createProfile(any(TraineeRegistrationRequest.class)))
                .thenReturn(mockRegResponse);

        when(trainerService.selectTrainerProfile(101L))
                .thenReturn(Optional.of(mockTrainerResponse));

        // --- ACT ---
        runner.run(null);

        // --- ASSERT ---
        // Verify the logic inside CrmDemoRunner was executed
        verify(traineeService, times(1)).createProfile(any(TraineeRegistrationRequest.class));
        verify(trainerService, times(1)).selectTrainerProfile(101L);

        // Cleanup security context after test
        SecurityContextHolder.clearContext();
    }
}