package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Training Service Unit Tests")
class TrainingServiceImplTest {

    @Mock private TrainingDao trainingDao;
    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;

    @InjectMocks private TrainingServiceImpl trainingService;

    private Training sampleTraining;
    private Trainee sampleTrainee;
    private Trainer sampleTrainer;

    @BeforeEach
    void setUp() {
        // Required for filtering logic that uses secondary DAOs
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);

        sampleTrainee = new Trainee();
        sampleTrainee.setUserId(1L);
        sampleTrainee.setUsername("john.doe");
        sampleTrainee.setFirstName("John");

        sampleTrainer = new Trainer();
        sampleTrainer.setUserId(10L);
        sampleTrainer.setUsername("coach.bob");
        sampleTrainer.setFirstName("Bob");

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Cardio");

        sampleTraining = new Training();
        sampleTraining.setId(100L);
        sampleTraining.setTrainingName("Morning Run");
        sampleTraining.setTrainingDate(LocalDate.of(2025, 1, 1));
        sampleTraining.setTraineeId(1L);
        sampleTrainer.setUserId(10L); // Ensure ID matches for the filter
        sampleTraining.setTrainerId(10L);
        sampleTraining.setTrainingType(type);
    }

    @Test
    @DisplayName("CREATE: Should successfully save a training session")
    void createProfile_Success() {
        // ARRANGE
        when(trainingDao.save(sampleTraining)).thenReturn(sampleTraining);

        // ACT
        Training result = trainingService.createProfile(sampleTraining);

        // ASSERT
        assertNotNull(result);
        assertEquals("Morning Run", result.getTrainingName());
        verify(trainingDao, times(1)).save(sampleTraining);
    }

    @Test
    @DisplayName("SELECT (ID): Should return empty Optional when training is not found")
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
    @DisplayName("SELECT (NAME): Should return Training profile when found by name")
    void selectProfileByName_Found() {
        // ARRANGE
        // This test targets the 'if (training.isPresent())' branch and logger.debug shown in the coverage report
        String name = "Morning Run";
        when(trainingDao.findByName(name)).thenReturn(Optional.of(sampleTraining));

        // ACT
        Optional<Training> result = trainingService.selectProfile(name);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(name, result.get().getTrainingName());
    }

    @Test
    @DisplayName("FILTER (TRAINEE): Should filter trainings by username and date range")
    void getTraineeTrainings_FilteredSuccess() {
        // ARRANGE
        String username = "john.doe";
        LocalDate from = LocalDate.of(2024, 12, 31);
        LocalDate to = LocalDate.of(2025, 1, 2);

        // Corrected stubbing: We return a List directly
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(traineeDao.findById(1L)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        List<Training> result = trainingService.getTraineeTrainings(username, from, to, null, "Cardio");

        // ASSERT
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Morning Run", result.get(0).getTrainingName());
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should filter trainings by trainer username")
    void getTrainerTrainings_FilteredSuccess() {
        // ARRANGE
        String trainerUsername = "coach.bob";
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(10L)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        List<Training> result = trainingService.getTrainerTrainings(trainerUsername, null, null, null);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("coach.bob", sampleTrainer.getUsername());
        verify(trainerDao, atLeastOnce()).findById(10L);
    }

    @Test
    @DisplayName("FILTER: Should return empty list if trainee username does not match")
    void getTraineeTrainings_NoMatch() {
        // ARRANGE
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(traineeDao.findById(1L)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        // Searching for a username that doesn't match the mocked trainee
        List<Training> result = trainingService.getTraineeTrainings("wrong.user", null, null, null, null);

        // ASSERT
        assertTrue(result.isEmpty());
    }
}