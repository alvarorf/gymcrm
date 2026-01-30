package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TrainerMapper;
import com.gymcrm.mapper.TrainingTypeMapper;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.interfaces.TrainingTypeService;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainer service unit tests")
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TraineeDao traineeDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private TrainingTypeService trainingTypeService;
    @Mock private TrainerMapper trainerMapper;

    @InjectMocks private TrainerServiceImpl trainerService;

    private Trainer sampleTrainer;
    private TrainingType mockType;
    private final String MOCK_USER = "dwight.schrute";
    private final String MOCK_PASS = "beetroot123";

    @BeforeEach
    void setUp() {
        // Manually trigger setter injection for mocks (since they are not final in the Service)
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setTraineeDao(traineeDao);
        trainerService.setTrainerMapper(trainerMapper);
        trainerService.setTrainingTypeService(trainingTypeService);

        mockType = new TrainingType();
        mockType.setTrainingTypeName("Martial Arts");

        sampleTrainer = new Trainer();
        sampleTrainer.setFirstName("Dwight");
        sampleTrainer.setLastName("Schrute");
        sampleTrainer.setUsername(MOCK_USER);
        sampleTrainer.setSpecialization(mockType);
        sampleTrainer.setActive(true);
    }

    @Test
    @DisplayName("CREATE: Should set generated credentials and return response")
    void createProfile_Success() {
        // ARRANGE
        // Create the DTO
        TrainingTypeRequest typeRequest = TrainingTypeRequest.builder()
                .trainingTypeName("Martial Arts")
                .build();

        // Use the mapper to resolve the required type: TrainingType
        TrainingTypeMapper tempMapper = new TrainingTypeMapper();
        TrainingType mappedType = tempMapper.toEntity(typeRequest);

        TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                .firstName("Dwight")
                .lastName("Schrute")
                .specialization(mappedType)
                .build();

        RegistrationResponse expectedResponse = new RegistrationResponse(MOCK_USER, MOCK_PASS);

        // Mocking for the Service logic
        when(trainingTypeService.findByName("Martial Arts")).thenReturn(Optional.of(mockType));
        when(trainerMapper.toEntity(eq(request), any(TrainingType.class))).thenReturn(sampleTrainer);
        when(usernameGenerator.generateUsername("Dwight", "Schrute")).thenReturn(MOCK_USER);
        when(passwordGenerator.generatePassword()).thenReturn(MOCK_PASS);
        when(trainerDao.save(any(Trainer.class))).thenReturn(sampleTrainer);
        when(trainerMapper.toRegistrationResponse(sampleTrainer)).thenReturn(expectedResponse);

        // ACT
        RegistrationResponse result = trainerService.createProfile(request);

        // ASSERT
        assertAll("Verify profile creation",
                () -> assertNotNull(result),
                () -> assertEquals(MOCK_USER, result.getUsername()),
                () -> assertEquals(MOCK_PASS, result.getPassword())
        );
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    @DisplayName("UPDATE: Should update entity and return profile response")
    void updateProfile_Success() {
        // ARRANGE
        TrainerUpdateRequest request = TrainerUpdateRequest.builder()
                .username(MOCK_USER)
                .firstName("Dwight")
                .lastName("Schrute")
                .isActive(true)
                .build();

        TrainerProfileResponse expectedResponse = TrainerProfileResponse.builder()
                .firstName("Dwight")
                .lastName("Schrute")
                .isActive(true)
                .build();

        when(trainerDao.findByUsername(MOCK_USER)).thenReturn(Optional.of(sampleTrainer));
        when(trainerDao.save(sampleTrainer)).thenReturn(sampleTrainer);
        when(trainerMapper.toProfileResponse(sampleTrainer)).thenReturn(expectedResponse);

        // ACT
        TrainerProfileResponse result = trainerService.updateProfile(request);

        // ASSERT
        assertNotNull(result);
        assertEquals("Dwight", result.getFirstName());
        verify(trainerMapper).updateEntityFromRequest(request, sampleTrainer);
        verify(trainerDao).save(sampleTrainer);
    }

    @Test
    @DisplayName("GET UNASSIGNED: Should filter out trainers already assigned and return ShortResponse list")
    void getUnassignedActiveTrainers_Success() {
        // ARRANGE
        String traineeUsername = "jim.halpert";
        Trainee jim = new Trainee();
        jim.setTrainers(new HashSet<>());

        Trainer Michael = new Trainer();
        Michael.setUsername("michael.scott");
        Michael.setFirstName("Michael");
        Michael.setActive(true);

        TrainerShortResponse expectedDto = TrainerShortResponse.builder()
                .username("michael.scott")
                .firstName("Michael")
                .build();

        when(traineeDao.findByUsername(traineeUsername)).thenReturn(Optional.of(jim));
        when(trainerDao.findAll()).thenReturn(List.of(sampleTrainer, Michael));
        when(trainerMapper.toShortResponse(Michael)).thenReturn(expectedDto);
        // sampleTrainer (Dwight) will be filtered out if we add him to Jim's list in the test setup
        jim.getTrainers().add(sampleTrainer);

        // ACT
        List<TrainerShortResponse> result = trainerService.getUnassignedActiveTrainersByTraineeUsername(traineeUsername);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("michael.scott", result.get(0).getUsername());
        // Verify Dwight is not in the list by checking usernames
        boolean containsDwight = result.stream().anyMatch(dto -> dto.getUsername().equals(MOCK_USER));
        assertFalse(containsDwight, "The list should not contain the assigned trainer Dwight");
    }

    @Test
    @DisplayName("TOGGLE ACTIVATION: Should flip activation status and save")
    void toggleActivation_Success() {
        // ARRANGE
        sampleTrainer.setActive(true);
        when(trainerDao.findByUsername(MOCK_USER)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.toggleActivation(MOCK_USER);

        // ASSERT
        assertFalse(sampleTrainer.isActive());
        verify(trainerDao).save(sampleTrainer);
    }
}