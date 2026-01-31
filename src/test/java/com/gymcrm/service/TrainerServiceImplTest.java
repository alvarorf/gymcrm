package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TrainerMapper;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.interfaces.TrainingTypeService;
import com.gymcrm.util.CredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainer Service Unit Tests")
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TrainerMapper trainerMapper;
    @Mock private CredentialsGenerator credentialsGenerator;
    @Mock private TraineeDao traineeDao;
    @Mock private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer sampleTrainer;
    private TrainingType mockType;
    private final String USERNAME = "seth.rollins";

    @BeforeEach
    void setUp() {
        // Manually injecting dependencies that use @Setter in the service
        trainerService.setCredentialsGenerator(credentialsGenerator);
        trainerService.setTraineeDao(traineeDao);
        trainerService.setTrainingTypeService(trainingTypeService);

        mockType = new TrainingType();
        mockType.setTrainingTypeName("Strength");

        sampleTrainer = new Trainer();
        sampleTrainer.setFirstName("Seth");
        sampleTrainer.setLastName("Rollins");
        sampleTrainer.setUsername(USERNAME);
        sampleTrainer.setSpecialization(mockType);
        sampleTrainer.setActive(true);
    }

    @Test
    @DisplayName("CREATE: Should return raw credentials when registration is successful")
    void createProfile_Success() {
        // ARRANGE
        TrainingTypeRequest typeRequest = new TrainingTypeRequest("Strength");
        TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                .firstName("Seth")
                .lastName("Rollins")
                .specialization(typeRequest)
                .build();

        GeneratedCredentialsResponse mockCredentials = new GeneratedCredentialsResponse(USERNAME, "plainPass123", "hashedPass789");

        when(trainingTypeService.findByName("Strength")).thenReturn(Optional.of(mockType));
        when(trainerMapper.toEntity(eq(request), any(TrainingType.class))).thenReturn(sampleTrainer);
        when(credentialsGenerator.generate(anyString(), anyString())).thenReturn(mockCredentials);
        when(trainerDao.save(any(Trainer.class))).thenReturn(sampleTrainer);

        // ACT
        RegistrationResponse result = trainerService.createProfile(request);

        // ASSERT
        assertAll("Verify Registration Output",
                () -> assertEquals(USERNAME, result.getUsername()),
                () -> assertEquals("plainPass123", result.getPassword()),
                () -> assertEquals("hashedPass789", sampleTrainer.getPassword())
        );
        verify(trainerDao).save(sampleTrainer);
    }

    @Test
    @DisplayName("UPDATE: Should update existing trainer and return profile response")
    void updateProfile_Success() {
        // ARRANGE
        TrainerUpdateRequest request = TrainerUpdateRequest.builder()
                .username(USERNAME)
                .firstName("Colby") // Changed name
                .isActive(false)
                .build();

        TrainerProfileResponse expectedDto = TrainerProfileResponse.builder().firstName("Colby").isActive(false).build();

        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(sampleTrainer));
        when(trainerDao.save(sampleTrainer)).thenReturn(sampleTrainer);
        when(trainerMapper.toProfileResponse(sampleTrainer)).thenReturn(expectedDto);

        // ACT
        TrainerProfileResponse result = trainerService.updateProfile(request);

        // ASSERT
        assertNotNull(result);
        assertEquals("Colby", result.getFirstName());
        verify(trainerMapper).updateEntityFromRequest(request, sampleTrainer);
        verify(trainerDao).save(sampleTrainer);
    }

    @Test
    @DisplayName("SELECT: Should return empty Optional if username not found")
    void selectTrainerProfile_NotFound() {
        // ARRANGE
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        // ACT
        Optional<TrainerProfileResponse> result = trainerService.selectTrainerProfile("unknown");

        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("TOGGLE: Should switch active status from true to false")
    void toggleActivation_ByUsername_Success() {
        // ARRANGE
        sampleTrainer.setActive(true);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.toggleActivation(USERNAME);

        // ASSERT
        assertFalse(sampleTrainer.isActive());
        verify(trainerDao).save(sampleTrainer);
    }

    @Test
    @DisplayName("UNASSIGNED LIST: Should return only active trainers not assigned to specific trainee")
    void getUnassignedActiveTrainers_FilteringLogic() {
        // ARRANGE
        String traineeUser = "roman.reigns";
        Trainee trainee = new Trainee();

        // 1. Assigned Trainer
        Trainer assigned = new Trainer();
        assigned.setUsername("dean.ambrose");
        assigned.setActive(true);
        trainee.setTrainers(new HashSet<>(Collections.singletonList(assigned)));

        // 2. Active Unassigned (The one we want)
        Trainer activeUnassigned = sampleTrainer;

        // 3. Inactive Unassigned
        Trainer inactive = new Trainer();
        inactive.setUsername("triple.h");
        inactive.setActive(false);

        when(traineeDao.findByUsername(traineeUser)).thenReturn(Optional.of(trainee));
        when(trainerDao.findAll()).thenReturn(Arrays.asList(assigned, activeUnassigned, inactive));

        TrainerShortResponse expectedDto = new TrainerShortResponse(USERNAME, "Seth", "Rollins", null);
        when(trainerMapper.toShortResponse(activeUnassigned)).thenReturn(expectedDto);

        // ACT
        List<TrainerShortResponse> result = trainerService.getUnassignedActiveTrainersByTraineeUsername(traineeUser);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals(USERNAME, result.get(0).getUsername());
        verify(trainerMapper, times(1)).toShortResponse(any());
    }

    @Test
    @DisplayName("UPDATE PASSWORD: Should save new password when ID is valid")
    void updatePassword_Success() {
        // ARRANGE
        Long id = 100L;
        when(trainerDao.findById(id)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.updatePassword(id, "newSecurePass");

        // ASSERT
        assertEquals("newSecurePass", sampleTrainer.getPassword());
        verify(trainerDao).save(sampleTrainer);
    }

    @Test
    @DisplayName("SELECT ID: Should return profile response when ID is found")
    void selectTrainerProfile_ById_Success() {
        // ARRANGE
        Long trainerId = 1L;
        TrainerProfileResponse expectedResponse = TrainerProfileResponse.builder()
                .firstName("Seth")
                .isActive(true)
                .build();

        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(sampleTrainer));
        when(trainerMapper.toProfileResponse(sampleTrainer)).thenReturn(expectedResponse);

        // ACT
        Optional<TrainerProfileResponse> result = trainerService.selectTrainerProfile(trainerId);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Seth", result.get().getFirstName());
        verify(trainerDao).findById(trainerId);
        verify(trainerMapper).toProfileResponse(sampleTrainer);
    }

    @Test
    @DisplayName("TOGGLE ID: Should flip active status from true to false via ID")
    void toggleActivation_ById_Success() {
        // ARRANGE
        Long trainerId = 1L;
        sampleTrainer.setActive(true);
        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.toggleActivation(trainerId);

        // ASSERT
        assertFalse(sampleTrainer.isActive(), "Trainer status should be toggled to false");
        verify(trainerDao).findById(trainerId);
        verify(trainerDao).save(sampleTrainer);
    }

    @Test
    @DisplayName("TOGGLE ID: Should flip active status from false to true via ID")
    void toggleActivation_ById_FlipBackSuccess() {
        // ARRANGE
        Long trainerId = 1L;
        sampleTrainer.setActive(false);
        when(trainerDao.findById(trainerId)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.toggleActivation(trainerId);

        // ASSERT
        assertTrue(sampleTrainer.isActive(), "Trainer status should be toggled to true");
        verify(trainerDao).save(sampleTrainer);
    }
}