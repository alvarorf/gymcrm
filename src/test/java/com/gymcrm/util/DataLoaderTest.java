package com.gymcrm.util;

import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.repositories.TrainerRepository;
import com.gymcrm.repositories.TrainingRepository;
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

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;

    @BeforeEach
    void setUp() {
        // ARRANGE
        dataLoader = new DataLoader(traineeRepository, trainerRepository, trainingRepository);
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
        verify(trainerRepository, never()).saveAll(anyList());
        verify(traineeRepository, never()).saveAll(anyList());
        verify(trainingRepository, never()).saveAll(anyList());
    }
}