package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainee service core business logic tests")
class TraineeServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks private TraineeServiceImpl traineeService;

    private Trainee sampleTrainee;
    private final Long TEST_ID = 5L;

    @BeforeEach
    void setUp() {
        // Resolve Mockito setter injection issue
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);

        sampleTrainee = new Trainee();
        sampleTrainee.setFirstName("Jane");
        sampleTrainee.setLastName("Doe");
        sampleTrainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        sampleTrainee.setAddress("101 Mock Ave");
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
}