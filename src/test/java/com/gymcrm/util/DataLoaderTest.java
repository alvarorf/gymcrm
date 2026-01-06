package com.gymcrm.util;

import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.repositories.TrainerRepository;
import com.gymcrm.repositories.TrainingRepository;
import com.gymcrm.repositories.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataLoaderTest {

    private DataLoader dataLoader;

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;

    @BeforeEach
    void setUp() {
        // ARRANGE
        dataLoader =
                new DataLoader(traineeRepository, trainerRepository, trainingRepository, trainingTypeRepository);
    }

    @Test
    @DisplayName("1. LOAD: Should persist all entities to DB when valid JSON path is provided.")
    void loadInitialData_shouldPersistToAllRepositories() {
        // ARRANGE
        String validPath = "initial-data/initial-data.json";

        // ACT
        dataLoader.loadInitialData(validPath);

        // ASSERT
        // Verify that each repository saveAll method was called
        verify(trainingTypeRepository, times(1)).saveAll(anyList());
        verify(trainerRepository, times(1)).saveAll(anyList());
        verify(traineeRepository, times(1)).saveAll(anyList());
        verify(trainingRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("2. ERROR: Should not interact with repositories when file path is invalid.")
    void loadInitialData_shouldHandleFileNotFound() {
        // ARRANGE
        String invalidPath = "invalid/path.json";

        // ACT
        dataLoader.loadInitialData(invalidPath);

        // ASSERT
        // Verify no save operations were attempted
        verify(trainingTypeRepository, never()).saveAll(anyList());
        verify(trainerRepository, never()).saveAll(anyList());
        verify(traineeRepository, never()).saveAll(anyList());
        verify(trainingRepository, never()).saveAll(anyList());
    }
}