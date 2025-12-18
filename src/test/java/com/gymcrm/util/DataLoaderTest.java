package com.gymcrm.util;

import com.gymcrm.model.Trainee;
import com.gymcrm.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataLoaderTest {

    private DataLoader dataLoader;

    @Mock
    private Storage storage;

    private Map<Long, Trainee> traineeMap;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader();
        traineeMap = new HashMap<>();
    }

    @Test
    @DisplayName("1. LOAD: Should populate storage maps when valid JSON path is provided.")
    void loadInitialData_shouldPopulateMaps_whenValidPath() {
        // ARRANGE
        // Path must include the sub-folder as defined in resources
        String validPath = "initial-data/initial-data.json";
        when(storage.getTraineeStorageMap()).thenReturn(traineeMap);
        when(storage.getTrainerStorageMap()).thenReturn(new HashMap<>());
        when(storage.getTrainingStorageMap()).thenReturn(new HashMap<>());

        // ACT
        dataLoader.loadInitialData(storage, validPath);

        // ASSERT
        assertFalse(traineeMap.isEmpty(), "Trainee map should not be empty after loading valid data");
        assertTrue(traineeMap.containsKey(201L), "Trainee with ID 201 should exist in storage");
        verify(storage, atLeastOnce()).getTraineeStorageMap();
    }

    @Test
    @DisplayName("2. ERROR: Should handle non-existent file path gracefully without throwing exception.")
    void loadInitialData_shouldHandleFileNotFound() {
        // ARRANGE
        String invalidPath = "wrong-folder/missing-file.json";

        // ACT
        dataLoader.loadInitialData(storage, invalidPath);

        // ASSERT
        assertTrue(traineeMap.isEmpty(), "Maps should remain empty when file is not found");
    }
}