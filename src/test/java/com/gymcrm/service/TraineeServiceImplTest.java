package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TraineeMapper;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.util.CredentialsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainee Service Unit Tests")
class TraineeServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TraineeMapper traineeMapper;
    @Mock private TrainerDao trainerDao; // Setter injected
    @Mock private CredentialsGenerator credentialsGenerator; // Setter injected

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee sampleTrainee;
    private final String USERNAME = "john.doe";

    @BeforeEach
    void setUp() {
        // Handle setter injection for mocks
        traineeService.setCredentialsGenerator(credentialsGenerator);
        traineeService.setTrainerDao(trainerDao);

        sampleTrainee = new Trainee();
        sampleTrainee.setUserId(1L);
        sampleTrainee.setFirstName("John");
        sampleTrainee.setLastName("Doe");
        sampleTrainee.setUsername(USERNAME);
        sampleTrainee.setActive(true);
    }

    @Test
    @DisplayName("CREATE: Should generate credentials, hash password, and return registration response")
    void createProfile_Success() {
        // ARRANGE
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(1995, 1, 1), "123 Gym St"
        ); // TODO: Fix error: no suitable constructor found for TraineeRegistrationRequest(java.lang.String,java.lang.String,java.time.LocalDate,java.lang.String)
        GeneratedCredentialsResponse mockCreds = new GeneratedCredentialsResponse(
                USERNAME, "rawPass123", "hashedPass789"
        );

        when(traineeMapper.toEntity(request)).thenReturn(sampleTrainee);
        when(credentialsGenerator.generate("John", "Doe")).thenReturn(mockCreds);
        when(traineeDao.save(any(Trainee.class))).thenReturn(sampleTrainee);

        // ACT
        RegistrationResponse result = traineeService.createProfile(request);

        // ASSERT
        assertAll("Verify trainee creation logic",
                () -> assertEquals(USERNAME, result.getUsername(), "Username should match generated"),
                () -> assertEquals("rawPass123", result.getPassword(), "Should return RAW password to user"),
                () -> assertEquals("hashedPass789", sampleTrainee.getPassword(), "Should store HASHED password in entity")
        );
        verify(traineeDao).save(sampleTrainee);
    }

    @Test
    @DisplayName("UPDATE: Should find existing trainee and apply updates via mapper")
    void updateProfile_Success() {
        // ARRANGE
        TraineeUpdateRequest request = TraineeUpdateRequest.builder()
                .username(USERNAME)
                .firstName("John Updated")
                .isActive(true)
                .build();
        TraineeProfileResponse expectedResponse = TraineeProfileResponse.builder()
                .firstName("John Updated")
                .build();

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(sampleTrainee));
        when(traineeDao.save(sampleTrainee)).thenReturn(sampleTrainee);
        when(traineeMapper.toProfileResponse(sampleTrainee)).thenReturn(expectedResponse);

        // ACT
        TraineeProfileResponse result = traineeService.updateProfile(request);

        // ASSERT
        assertNotNull(result);
        verify(traineeMapper).updateEntityFromRequest(request, sampleTrainee);
        verify(traineeDao).save(sampleTrainee);
    }

    @Test
    @DisplayName("DELETE: Should delete by username if trainee exists")
    void deleteProfile_ByUsername_Success() {
        // ARRANGE
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        traineeService.deleteProfile(USERNAME);

        // ASSERT
        verify(traineeDao).delete(sampleTrainee.getUserId());
    }

    @Test
    @DisplayName("TOGGLE: Should flip activation status from active to inactive")
    void toggleActivation_Success() {
        // ARRANGE
        sampleTrainee.setActive(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        traineeService.toggleActivation(USERNAME);

        // ASSERT
        assertFalse(sampleTrainee.isActive(), "Status should have flipped to false");
        verify(traineeDao).save(sampleTrainee);
    }

    @Test
    @DisplayName("UPDATE TRAINERS: Should replace trainee's trainer list with new entities")
    void updateTraineeTrainers_Success() {
        // ARRANGE
        List<String> trainerUsernames = List.of("trainer.one", "trainer.two");
        Trainer t1 = new Trainer(); t1.setUsername("trainer.one");
        Trainer t2 = new Trainer(); t2.setUsername("trainer.two");

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(sampleTrainee));
        when(trainerDao.findByUsername("trainer.one")).thenReturn(Optional.of(t1));
        when(trainerDao.findByUsername("trainer.two")).thenReturn(Optional.of(t2));

        // ACT
        traineeService.updateTraineeTrainers(USERNAME, trainerUsernames);

        // ASSERT
        assertEquals(2, sampleTrainee.getTrainers().size());
        assertTrue(sampleTrainee.getTrainers().contains(t1));
        verify(traineeDao).save(sampleTrainee);
    }

    @Test
    @DisplayName("SELECT: Should return profile response for valid ID")
    void selectProfile_ById_Success() {
        // ARRANGE
        Long id = 1L;
        TraineeProfileResponse response = TraineeProfileResponse.builder().firstName("John").build();
        when(traineeDao.findById(id)).thenReturn(Optional.of(sampleTrainee));
        when(traineeMapper.toProfileResponse(sampleTrainee)).thenReturn(response);

        // ACT
        Optional<TraineeProfileResponse> result = traineeService.selectProfile(id);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }
}