package com.gymcrm.dao;

import com.gymcrm.model.Training;
import com.gymcrm.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoImplTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    private Map<Long, Training> trainingMap;

    @BeforeEach
    void setUp() {
        trainingMap = new HashMap<>();
        lenient().when(storage.getTrainingStorageMap()).thenReturn(trainingMap);
    }

    @Test
    @DisplayName("1. SAVE: Should generate new ID and save when training ID is null.")
    void save_shouldGenerateNewId_whenIdIsNull() {
        // ARRANGE
        Training training = new Training();
        training.setTrainingName("Yoga Session");
        when(storage.getNextTrainingId()).thenReturn(500L);

        // ACT
        Training savedTraining = trainingDao.save(training);

        // ASSERT
        assertEquals(500L, savedTraining.getId());
        assertTrue(trainingMap.containsKey(500L));
    }

    @Test
    @DisplayName("2. SELECT: Should return Optional empty when training ID does not exist.")
    void findById_shouldReturnOptionalEmpty_whenIdDoesNotExist() {
        // ARRANGE
        Long id = 999L;

        // ACT
        Optional<Training> result = trainingDao.findById(id);

        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("3. SELECT ALL: Should return a list containing all trainings from storage.")
    void findAll_shouldReturnAllTrainings() {
        // ARRANGE
        Training t1 = new Training(); t1.setId(501L);
        Training t2 = new Training(); t2.setId(502L);
        trainingMap.put(501L, t1);
        trainingMap.put(502L, t2);

        // ACT
        List<Training> result = trainingDao.findAll();

        // ASSERT
        assertEquals(2, result.size());
        verify(storage, atLeastOnce()).getTrainingStorageMap();
    }
}