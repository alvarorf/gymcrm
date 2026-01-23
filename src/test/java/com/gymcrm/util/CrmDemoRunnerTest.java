package com.gymcrm.util;

import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrmDemoRunnerTest {

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Test
    @DisplayName("1. DEMO RUNNER: Should call createProfile and selectProfile through the Facade.")
    void runDemo_shouldExecuteWorkflow() {
        // ARRANGE
        when(gymFacade.getTraineeService()).thenReturn(traineeService);
        when(gymFacade.getTrainerService()).thenReturn(trainerService);

        TrainingType motivationType = new TrainingType();
        motivationType.setTrainingTypeName("Motivation");

        Trainee mockTrainee = Trainee.builder()
                .username("john.doe")
                .build();

        Trainer mockTrainer = Trainer.builder()
                .firstName("Michael")
                .lastName("Scott")
                .specialization(motivationType)
                .build();

        when(traineeService.createProfile(any(Trainee.class)))
                .thenReturn(mockTrainee);

        when(trainerService.selectProfile(101L))
                .thenReturn(Optional.of(mockTrainer));

        // Inject mock facade
        CrmDemoRunner runner = new CrmDemoRunner(gymFacade);

        // ACT
        runner.run(null);

        // ASSERT
        verify(traineeService, times(1)).createProfile(any(Trainee.class));
        verify(trainerService, times(1)).selectProfile(101L);

        // Cleanup security context after test
        SecurityContextHolder.clearContext();
    }
}