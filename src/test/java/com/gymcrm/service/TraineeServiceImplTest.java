package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
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

import java.time.LocalDate;
import java.util.Arrays;
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

    @InjectMocks private TraineeServiceImpl traineeService;

    private Trainee sampleTrainee;
    private final Long TEST_ID = 5L;
    private final String TEST_USERNAME = "jane.doe";

    @BeforeEach
    void setUp() {
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setTrainerDao(trainerDao);

        sampleTrainee = new Trainee();
        sampleTrainee.setUserId(TEST_ID);
        sampleTrainee.setFirstName("Jane");
        sampleTrainee.setLastName("Doe");
        sampleTrainee.setUsername(TEST_USERNAME);
        sampleTrainee.setPassword("oldPassword");
        sampleTrainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        sampleTrainee.setAddress("101 Mock Ave");
        sampleTrainee.setActive(true);
    }

    @Test
    @DisplayName("1. CREATE: Should generate unique credentials and set isActive=true.")
    void createProfile_GeneratesCredentialsAndSaves() {
        // ARRANGE
        String MOCK_USERNAME = "jane.doe";
        String MOCK_PASSWORD = "testPassword123";

        when(usernameGenerator.generateUsername(anyString(), anyString())).thenReturn(MOCK_USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(MOCK_PASSWORD);

        Trainee savedTrainee = new Trainee();
        savedTrainee.setUserId(TEST_ID);
        when(traineeDao.save(any(Trainee.class))).thenReturn(savedTrainee);

        // ACT
        Trainee result = traineeService.createProfile(sampleTrainee);

        // ASSERT
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao, times(1)).save(traineeCaptor.capture());

        Trainee capturedTrainee = traineeCaptor.getValue();
        assertEquals(MOCK_USERNAME, capturedTrainee.getUsername());
        assertEquals(MOCK_PASSWORD, capturedTrainee.getPassword());
        assertTrue(capturedTrainee.isActive());
        assertEquals(TEST_ID, result.getUserId());
    }

    @Test
    @DisplayName("2. UPDATE: Should pass existing Trainee object to DAO.")
    void updateProfile_Success() {
        // ARRANGE
        sampleTrainee.setUserId(TEST_ID);
        when(traineeDao.save(any(Trainee.class))).thenReturn(sampleTrainee);

        // ACT
        Trainee result = traineeService.updateProfile(sampleTrainee);

        // ASSERT
        verify(traineeDao, times(1)).save(sampleTrainee);
        assertEquals(TEST_ID, result.getUserId());
    }

    @Test
    @DisplayName("3. SELECT: Should return empty Optional when not found.")
    void selectProfile_NotFound() {
        // ARRANGE
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        // ACT
        Optional<Trainee> result = traineeService.selectProfile(999L);

        // ASSERT
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("4. DELETE: Should call delete method on DAO.")
    void deleteProfile_Success() {
        // ARRANGE
        Long idToDelete = 10L;

        // ACT
        traineeService.deleteProfile(idToDelete);

        // ASSERT
        verify(traineeDao, times(1)).delete(idToDelete);
    }

    @Test
    @DisplayName("5. AUTHENTICATE: Should return true if credentials match.")
    void authenticate_Success() {
        // ARRANGE
        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        boolean result = traineeService.authenticate(TEST_USERNAME, "oldPassword");

        // ASSERT
        assertTrue(result);
        verify(traineeDao, times(1)).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("6. SELECT (USERNAME): Should find profile by username.")
    void selectProfile_ByUsername() {
        // ARRANGE
        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        Optional<Trainee> result = traineeService.selectProfile(TEST_USERNAME);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(TEST_USERNAME, result.get().getUsername());
    }

    @Test
    @DisplayName("7. DELETE (USERNAME): Should find user then call delete by ID.")
    void deleteProfile_ByUsername() {
        // ARRANGE
        when(traineeDao.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        traineeService.deleteProfile(TEST_USERNAME);

        // ASSERT
        verify(traineeDao, times(1)).delete(TEST_ID);
    }

    @Test
    @DisplayName("8. UPDATE PASSWORD: Should update password field and save via DAO.")
    void updatePassword_Success() {
        // ARRANGE
        String newPass = "newSecurePass";
        when(traineeDao.findById(TEST_ID)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        traineeService.updatePassword(TEST_ID, newPass);

        // ASSERT
        assertEquals(newPass, sampleTrainee.getPassword());
        verify(traineeDao, times(1)).save(sampleTrainee);
    }

    @Test
    @DisplayName("9. TOGGLE ACTIVATION: Should flip the isActive status.")
    void toggleActivation_Success() {
        // ARRANGE
        sampleTrainee.setActive(true);
        when(traineeDao.findById(TEST_ID)).thenReturn(Optional.of(sampleTrainee));

        // ACT
        traineeService.toggleActivation(TEST_ID);

        // ASSERT
        assertFalse(sampleTrainee.isActive());
        verify(traineeDao, times(1)).save(sampleTrainee);
    }

    @Test
    @DisplayName("10. UPDATE TRAINERS: Should update the set of associated trainers.")
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
        assertEquals(1, sampleTrainee.getTrainers().size());
        assertTrue(sampleTrainee.getTrainers().contains(mockTrainer));
        verify(traineeDao, times(1)).save(sampleTrainee);
    }
}