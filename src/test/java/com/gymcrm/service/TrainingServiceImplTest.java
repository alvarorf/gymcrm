package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Training Service Unit Tests")
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    @DisplayName("CREATE: Should successfully save a training session")
    void createProfile_Success() {
        // ARRANGE
        Training training = new Training();
        training.setTrainingName("Yoga Basic");
        when(trainingDao.save(training)).thenReturn(training);

        // ACT
        Training result = trainingService.createProfile(training);

        // ASSERT
        assertNotNull(result);
        assertEquals("Yoga Basic", result.getTrainingName());
        verify(trainingDao, times(1)).save(training);
    }

    @Test
    @DisplayName("SELECT: Should return empty Optional when training is not found")
    void selectProfile_NotFound() {
        // ARRANGE
        // This test targets the 'else' branch and logger.warn shown in the coverage report
        Long nonExistentId = 99L;
        when(trainingDao.findById(nonExistentId)).thenReturn(Optional.empty());

        // ACT
        Optional<Training> result = trainingService.selectProfile(nonExistentId);

        // ASSERT
        assertFalse(result.isPresent());
        verify(trainingDao, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("SELECT: Should return Training profile when training is found")
    void selectProfile_Found() {
        // ARRANGE
        // This test targets the 'if (training.isPresent())' branch and logger.debug shown in the coverage report
        Long trainingId = 1L;
        Training training = new Training();
        training.setId(trainingId);
        training.setTrainingName("Weightlifting");

        when(trainingDao.findById(trainingId)).thenReturn(Optional.of(training));

        // ACT
        Optional<Training> result = trainingService.selectProfile(trainingId);

        // ASSERT
        assertTrue(result.isPresent(), "Training should be present in the result");
        assertEquals("Weightlifting", result.get().getTrainingName());
        verify(trainingDao, times(1)).findById(trainingId);
    }
}
