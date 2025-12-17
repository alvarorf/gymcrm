package com.gymcrm.facade;

import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Gym Facade Integration Tests")
class GymFacadeTest {

    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        // ARRANGE: Initialize the facade
        gymFacade = new GymFacade();
    }

    @Test
    @DisplayName("Should successfully retrieve TraineeService from context")
    void getTraineeService_ReturnsNotNull() {
        // ACT: Retrieve the bean
        TraineeService service = gymFacade.getTraineeService();

        // ASSERT: Verify the service is wired
        assertNotNull(service, "TraineeService should not be null when retrieved from Facade.");
    }

    @Test
    @DisplayName("Should successfully retrieve TrainerService from context")
    void getTrainerService_ReturnsNotNull() {
        // ACT
        TrainerService service = gymFacade.getTrainerService();

        // ASSERT
        assertNotNull(service, "TrainerService should not be null.");
    }

    @Test
    @DisplayName("Should successfully retrieve TrainingService from context")
    void getTrainingService_ReturnsNotNull() {
        // ACT
        TrainingService service = gymFacade.getTrainingService();

        // ASSERT
        assertNotNull(service, "TrainingService should not be null.");
    }
}