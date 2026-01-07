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

    private TrainingServiceImpl trainingService;

    private Training sampleTraining;
    private Trainee sampleTrainee;
    private Trainer sampleTrainer;
    private final Long TRAINING_ID = 100L;

    @BeforeEach
    void setUp() {
        // Manual instantiation is significantly faster than @InjectMocks
        // because it avoids dynamic bytecode manipulation for setters.
        trainingService = new TrainingServiceImpl(trainingDao, traineeDao, trainerDao);

        // ARRANGE: Using SuperBuilder is faster and cleaner than chain-setters
        sampleTrainee = Trainee.builder()
                .userId(1L)
                .username("john.doe")
                .firstName("John")
                .build();

        sampleTrainer = Trainer.builder()
                .userId(10L)
                .username("coach.bob")
                .firstName("Bob")
                .build();

        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Cardio");

        sampleTraining = Training.builder()
                .id(TRAINING_ID)
                .trainingName("Morning Run")
                .trainingDate(LocalDate.of(2025, 1, 1))
                .trainee(sampleTrainee)
                .trainer(sampleTrainer)
                .trainingType(TrainingType.builder().trainingTypeName("Cardio").build())
                .build();
    }

    // --- CREATE PROFILE COVERAGE ---

    @Test
    @DisplayName("CREATE: Should successfully save a training session and return the saved profile")
    void createProfile_Success() {
        // ARRANGE
        when(trainingDao.save(sampleTraining)).thenReturn(sampleTraining);

        // ACT
        Training result = trainingService.createProfile(sampleTraining);

        // ASSERT
        assertNotNull(result, "The saved training should not be null.");
        assertEquals(TRAINING_ID, result.getId());
        assertEquals("Morning Run", result.getTrainingName());
        verify(trainingDao, times(1)).save(sampleTraining);
    }

    // --- SELECT PROFILE COVERAGE ---

    @Test
    @DisplayName("SELECT (ID): Should hit logger.debug when training is found")
    void selectProfile_Found_HitsDebugLog() {
        // ARRANGE
        when(trainingDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTraining));

        // ACT
        Optional<Training> result = trainingService.selectProfile(TRAINING_ID);

        // ASSERT
        assertTrue(result.isPresent());
        verify(trainingDao).findById(TRAINING_ID);
    }

    @Test
    @DisplayName("SELECT (ID): Should hit logger.warn when training is not found by ID")
    void selectProfile_NotFound_HitsWarnLog() {
        // ARRANGE
        Long nonExistentId = 999L;
        when(trainingDao.findById(nonExistentId)).thenReturn(Optional.empty());

        // ACT
        Optional<Training> result = trainingService.selectProfile(nonExistentId);

        // ASSERT
        assertTrue(result.isEmpty(), "Result should be empty for non-existent ID.");
        verify(trainingDao, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("SELECT (NAME): Should hit logger.debug when training is found by name")
    void selectProfileByName_Found_HitsDebugLog() {
        // ARRANGE
        String name = "Morning Run";
        when(trainingDao.findByName(name)).thenReturn(Optional.of(sampleTraining));

        // ACT
        Optional<Training> result = trainingService.selectProfile(name);

        // ASSERT
        assertTrue(result.isPresent(), "Training should be present when searching by valid name.");
        assertEquals(name, result.get().getTrainingName());
        verify(trainingDao, times(1)).findByName(name);
    }

    @Test
    @DisplayName("SELECT (NAME): Should hit logger.warn when training is not found")
    void selectProfileByName_NotFound_HitsWarnLog() {
        // ARRANGE
        String name = "Yoga";
        when(trainingDao.findByName(name)).thenReturn(Optional.empty());

        // ACT
        Optional<Training> result = trainingService.selectProfile(name);

        // ASSERT
        assertTrue(result.isEmpty());
        verify(trainingDao).findByName(name);
    }

    // --- GET TRAINEE TRAININGS COVERAGE ---

    @Test
    @DisplayName("FILTER (TRAINEE): Should cover date range, trainer name, and type filters")
    void getTraineeTrainings_FullFilterCoverage() {
        // ARRANGE
        String username = "john.doe";
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 1);

        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        // The service uses t.getId() (100L) to look up the trainee/trainer
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainee));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        List<Training> result = trainingService.getTraineeTrainings(username, from, to, "Bob", "Cardio");

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("Morning Run", result.get(0).getTrainingName());
    }

    @Test
    @DisplayName("FILTER (TRAINEE): Should return empty if date is outside range")
    void getTraineeTrainings_OutsideDateRange() {
        // ARRANGE
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainee));
        LocalDate futureDate = LocalDate.of(2099, 1, 1);

        // ACT
        List<Training> result = trainingService.getTraineeTrainings("john.doe", futureDate, null, null, null);

        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("FILTER (TRAINEE): Should return empty if training type does not match")
    void getTraineeTrainings_TypeMismatch() {
        // ARRANGE
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainee));
        // Type in sampleTraining is "Cardio"

        // ACT
        List<Training> result = trainingService.getTraineeTrainings("john.doe", null, null, null, "Yoga");

        // ASSERT
        assertTrue(result.isEmpty(), "Result should be empty when training type filter fails.");
    }

    @Test
    @DisplayName("FILTER (TRAINEE): Should include training exactly on the 'from' and 'to' dates")
    void getTraineeTrainings_ExactDateMatch() {
        // ARRANGE
        LocalDate exactDate = LocalDate.of(2025, 1, 1);
        sampleTraining.setTrainingDate(exactDate);

        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        List<Training> result = trainingService.getTraineeTrainings("john.doe", exactDate, exactDate, null, null);

        // ASSERT
        assertEquals(1, result.size(), "Should include training when dates are inclusive.");
    }

    // --- GET TRAINER TRAININGS COVERAGE ---

    @Test
    @DisplayName("FILTER (TRAINER): Should cover date range and trainee name filters")
    void getTrainerTrainings_FullFilterCoverage() {
        // ARRANGE
        String trainerUser = "coach.bob";
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        List<Training> result = trainingService.getTrainerTrainings(trainerUser, null, null, "John");

        // ASSERT
        assertFalse(result.isEmpty());
        assertEquals("John", sampleTrainee.getFirstName());
        verify(traineeDao).findById(TRAINING_ID);
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should return empty if trainee name does not match")
    void getTrainerTrainings_NoTraineeMatch() {
        // ARRANGE
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        List<Training> result = trainingService.getTrainerTrainings("coach.bob", null, null, "WrongName");

        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should return empty if date is before the 'from' range")
    void getTrainerTrainings_DateBeforeRange() {
        // ARRANGE
        LocalDate fromDate = LocalDate.of(2025, 2, 1); // Sample is 2025-01-01
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        List<Training> result = trainingService.getTrainerTrainings("coach.bob", fromDate, null, null);

        // ASSERT
        assertTrue(result.isEmpty(), "Should be filtered out because 2025-01-01 is before 2025-02-01");
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should return empty if date is after the 'to' range")
    void getTrainerTrainings_DateAfterRange() {
        // ARRANGE
        LocalDate toDate = LocalDate.of(2024, 12, 31);

        // Use List.of() (immutable) which is faster than ArrayList for single items
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        List<Training> result = trainingService.getTrainerTrainings("coach.bob", null, toDate, null);

        // ASSERT
        assertTrue(result.isEmpty());
        // Verify short-circuit: date filter happens BEFORE trainee filter
        verify(traineeDao, never()).findById(anyLong());
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should return empty if trainee is not found in DAO")
    void getTrainerTrainings_TraineeNotFoundInDao() {
        // ARRANGE
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));
        // Mock traineeDao to return empty, covering the .orElse(false) branch
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.empty());

        // ACT
        List<Training> result = trainingService.getTrainerTrainings("coach.bob", null, null, "John");

        // ASSERT
        assertTrue(result.isEmpty(), "Result should be empty if the trainee lookup fails.");
        verify(traineeDao).findById(TRAINING_ID);
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should handle case where traineeName is provided but trainee record is missing")
    void getTrainerTrainings_TraineeRecordMissing_FilterOut() {
        // ARRANGE
        String trainerUser = "coach.bob";
        String searchTrainee = "John";

        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        // Trainer lookup must succeed to reach the trainee filter
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));
        // Trainee lookup returns empty, covering the .orElse(false) branch when traineeName != null
        when(traineeDao.findById(TRAINING_ID)).thenReturn(Optional.empty());

        // ACT
        List<Training> result = trainingService.getTrainerTrainings(trainerUser, null, null, searchTrainee);

        // ASSERT
        assertTrue(result.isEmpty(), "Training should be filtered out if the associated trainee record cannot be found.");
        verify(traineeDao).findById(TRAINING_ID);
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should return training when traineeName is null (short-circuit)")
    void getTrainerTrainings_TraineeNameNull_ReturnsTraining() {
        // ARRANGE
        String trainerUser = "coach.bob";
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainerDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        // Passing null for traineeName triggers the short-circuit (traineeName == null)
        List<Training> result = trainingService.getTrainerTrainings(trainerUser, null, null, null);

        // ASSERT
        assertFalse(result.isEmpty(), "Should return training when no trainee filter is applied.");
        assertEquals(1, result.size());
        // Verify that traineeDao was NEVER called due to the || short-circuit
        verify(traineeDao, never()).findById(anyLong());
    }
}