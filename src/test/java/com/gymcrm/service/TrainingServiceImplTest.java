package com.gymcrm.service;

import com.gymcrm.dao.interfaces.*;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TrainingMapper;
import com.gymcrm.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Training Service unit tests")
class TrainingServiceImplTest {

    @Mock private TrainingDao trainingDao;
    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training sampleTraining;
    private Trainee sampleTrainee;
    private Trainer sampleTrainer;
    private TrainingType sampleType;
    private final Long TRAINING_ID = 100L;

    @BeforeEach
    void setUp() {
        // Since we use @InjectMocks, dependencies are injected automatically.
        // We just need to set the setters that aren't in the constructor if necessary.
        trainingService.setTrainingMapper(trainingMapper);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);

        sampleType = TrainingType.builder().trainingTypeName("Cardio").build();

        sampleTrainee = Trainee.builder()
                .userId(1L)
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .build();

        sampleTrainer = Trainer.builder()
                .userId(10L)
                .username("coach.bob")
                .firstName("Bob")
                .lastName("Builder")
                .specialization(sampleType)
                .build();

        sampleTraining = Training.builder()
                .id(TRAINING_ID)
                .trainingName("Morning Run")
                .trainingDate(LocalDate.of(2025, 1, 1))
                .trainee(sampleTrainee)
                .trainer(sampleTrainer)
                .trainingType(sampleType)
                .trainingDuration(60)
                .build();
    }

    @Test
    @DisplayName("CREATE: Should successfully map request to entity and save")
    void createProfile_Success() {
        // ARRANGE
        TrainingCreateRequest request = TrainingCreateRequest.builder()
                .trainingName("Morning Run")
                .traineeUsername("john.doe")
                .trainerUsername("coach.bob")
                .trainingDate(LocalDate.of(2025, 1, 1))
                .trainingDuration(60)
                .build();

        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(sampleTrainee));
        when(trainerDao.findByUsername("coach.bob")).thenReturn(Optional.of(sampleTrainer));
        when(trainingMapper.toEntity(eq(sampleTrainee), eq(sampleTrainer), eq(request))).thenReturn(sampleTraining);

        // ACT
        trainingService.createProfile(request);

        // ASSERT
        verify(trainingDao, times(1)).save(sampleTraining);
    }

    @Test
    @DisplayName("SELECT (ID): Should return the entity when found")
    void selectProfile_Found() {
        // ARRANGE
        when(trainingDao.findById(TRAINING_ID)).thenReturn(Optional.of(sampleTraining));

        // ACT
        Optional<Training> result = trainingService.selectProfile(TRAINING_ID);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Morning Run", result.get().getTrainingName());
    }

    @Test
    @DisplayName("FILTER (TRAINEE): Should return list of TraineeTrainingResponse")
    void getTraineeTrainings_Success() {
        // ARRANGE
        String username = "john.doe";
        TraineeTrainingResponse responseDto = TraineeTrainingResponse.builder()
                .trainingName("Morning Run")
                .build();

        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainingMapper.toTraineeTrainingResponse(sampleTraining)).thenReturn(responseDto);

        // ACT
        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(username, null, null, null, null);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("Morning Run", result.get(0).getTrainingName());
    }

    @Test
    @DisplayName("FILTER (TRAINEE): Should filter out trainings by date range")
    void getTraineeTrainings_DateFilter() {
        // ARRANGE
        LocalDate futureDate = LocalDate.of(2099, 1, 1);
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));

        // ACT
        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings("john.doe", futureDate, null, null, null);

        // ASSERT
        assertTrue(result.isEmpty(), "Training from 2025 should be filtered out by 2099 start date.");
        verify(trainingMapper, never()).toTraineeTrainingResponse(any());
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should return list of TrainerTrainingResponse")
    void getTrainerTrainings_Success() {
        // ARRANGE
        String trainerUser = "coach.bob";
        TrainerTrainingResponse responseDto = TrainerTrainingResponse.builder()
                .trainingName("Morning Run")
                .traineeName("John Doe")
                .build();

        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        when(trainingMapper.toTrainerTrainingResponse(sampleTraining)).thenReturn(responseDto);

        // ACT
        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(trainerUser, null, null, null);

        // ASSERT
        assertFalse(result.isEmpty());
        assertEquals("John Doe", result.get(0).getTraineeName());
    }

    @Test
    @DisplayName("FILTER (TRAINER): Should filter by trainee first name")
    void getTrainerTrainings_TraineeNameFilter() {
        // ARRANGE
        String trainerUser = "coach.bob";
        when(trainingDao.findAll()).thenReturn(List.of(sampleTraining));
        // sampleTrainee has firstName "John"

        // ACT
        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(trainerUser, null, null, "John");
        List<TrainerTrainingResponse> resultEmpty = trainingService.getTrainerTrainings(trainerUser, null, null, "Mike");

        // ASSERT
        verify(trainingMapper, times(1)).toTrainerTrainingResponse(sampleTraining);
        assertTrue(resultEmpty.isEmpty(), "Should be empty when trainee name doesn't match.");
    }
}