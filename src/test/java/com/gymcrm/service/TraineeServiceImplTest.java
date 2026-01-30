package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeProfileResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TraineeUpdateRequest;
import com.gymcrm.mapper.TraineeMapper;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.util.Nomenclature;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainee service core business logic tests")
class TraineeServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private TraineeMapper traineeMapper;

    @InjectMocks private TraineeServiceImpl traineeService;

    private Trainee sampleTrainee;
    private final Long TEST_ID = 5L;
    private final String TEST_USERNAME = "jane.doe";

    @BeforeEach
    void setUp() {
        // Setter injection for non-constructor dependencies
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setTrainerDao(trainerDao);

        sampleTrainee = new Trainee();
        sampleTrainee.setUserId(TEST_ID);
        sampleTrainee.setFirstName("Jane");
        sampleTrainee.setLastName("Doe");
        sampleTrainee.setUsername(TEST_USERNAME);
        sampleTrainee.setPassword("oldPassword");
        sampleTrainee.setTrainers(new java.util.HashSet<>());
        sampleTrainee.setActive(true);
    }

    @Test
    @DisplayName("1. CREATE: Should generate unique credentials and return RegistrationResponse")
    void createProfile_Success() {
        // ARRANGE
        TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                .firstName("Jane").lastName("Doe").build();
        RegistrationResponse expectedResponse = new RegistrationResponse(TEST_USERNAME, "testPass123");

        when(traineeMapper.toEntity(request)).thenReturn(sampleTrainee);
        when(usernameGenerator.generateUsername("Jane", "Doe")).thenReturn(TEST_USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn("testPass123");
        when(traineeDao.save(any(Trainee.class))).thenReturn(sampleTrainee);
        when(traineeMapper.toRegistrationResponse(sampleTrainee)).thenReturn(expectedResponse);

        // ACT
        RegistrationResponse result = traineeService.createProfile(request);

        // ASSERT
        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getUsername());
        verify(traineeDao).save(sampleTrainee);
    }

    @Test
    @DisplayName("2. UPDATE: Should update entity and return ProfileResponse")
    void updateProfile_Success() {
        // ARRANGE
        TraineeUpdateRequest request = TraineeUpdateRequest.builder()
                .username(TEST_USERNAME).firstName("Jane").lastName("Doe").isActive(true).build();
        TraineeProfileResponse expectedResponse = TraineeProfileResponse.builder().firstName("Jane").build();

        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(sampleTrainee);
        when(traineeMapper.toProfileResponse(sampleTrainee)).thenReturn(expectedResponse);

        // ACT
        TraineeProfileResponse result = traineeService.updateProfile(request);

        // ASSERT
        assertNotNull(result);
        verify(traineeMapper).updateEntityFromRequest(eq(request), eq(sampleTrainee));
        verify(traineeDao).save(sampleTrainee);
    }

    @Test
    @DisplayName("3. SELECT (ID): Should return ProfileResponse when found")
    void selectProfile_Found() {
        // ARRANGE
        TraineeProfileResponse expectedResponse = TraineeProfileResponse.builder().firstName("Jane").build();
        when(traineeDao.findById(TEST_ID)).thenReturn(Optional.of(sampleTrainee));
        when(traineeMapper.toProfileResponse(sampleTrainee)).thenReturn(expectedResponse);

        // ACT
        Optional<TraineeProfileResponse> result = traineeService.selectProfile(TEST_ID);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());
    }

    @Test
    @DisplayName("4. SELECT (USERNAME): Should find profile by username")
    void selectTraineeProfile_Success() {
        // ARRANGE
        TraineeProfileResponse expectedResponse = TraineeProfileResponse.builder().firstName("Jane").build();
        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));
        when(traineeMapper.toProfileResponse(sampleTrainee)).thenReturn(expectedResponse);

        // ACT
        Optional<TraineeProfileResponse> result = traineeService.selectTraineeProfile(TEST_USERNAME);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Jane", result.get().getFirstName());
    }

    @Test
    @DisplayName("5. DELETE (USERNAME): Should find user then call delete by ID")
    void deleteProfile_ByUsername() {
        // ARRANGE
        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        traineeService.deleteProfile(TEST_USERNAME);

        // ASSERT
        verify(traineeDao).delete(TEST_ID);
    }

    @Test
    @DisplayName("6. UPDATE TRAINERS: Should update the set of associated trainers")
    void updateTraineeTrainers_Success() {
        // ARRANGE
        String trainerUser = "coach.bob";
        Trainer mockTrainer = new Trainer();
        mockTrainer.setUsername(trainerUser);

        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));
        when(trainerDao.findByUsername(trainerUser)).thenReturn(Optional.of(mockTrainer));

        // ACT
        traineeService.updateTraineeTrainers(TEST_USERNAME, List.of(trainerUser));

        // ASSERT
        verify(traineeDao).save(sampleTrainee);
        assertTrue(sampleTrainee.getTrainers().contains(mockTrainer));
    }
}