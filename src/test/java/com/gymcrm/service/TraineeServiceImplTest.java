package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.util.UsernamePasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// FIX: We can add the LENIENT flag to the test class itself to resolve unnecessary stubbing across all mocks
@ExtendWith(MockitoExtension.class)
@DisplayName("Trainee service core business logic tests")
class TraineeServiceImplTest {

    private static final Logger testLogger = LoggerFactory.getLogger(TraineeServiceImplTest.class);

    @Mock
    private TraineeDao traineeDao;
    @Mock
    private UsernamePasswordGenerator generator; // The dependency failing to inject

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee sampleTrainee;
    private final Long TEST_ID = 5L;

    @BeforeEach
    void setUp() {
        testLogger.info("--- Setting up test case ---");
        // Manually call the setter method to inject the mock 'generator' into the 'traineeService' instance.
        // This bypasses Mockito's failure to auto-inject via setter.
        traineeService.setGenerator(generator);
        sampleTrainee = new Trainee();
        sampleTrainee.setFirstName("Jane");
        sampleTrainee.setLastName("Doe");
        sampleTrainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        sampleTrainee.setAddress("101 Mock Ave");

        // Resolve UnnecessaryStubbingException: Move the 'generator' stubbing
        // ONLY into the test that uses it (createProfile_GeneratesCredentialsAndSaves).
        // Since other tests (update, select, delete) don't use the generator,
        // stubbing it here makes it 'unnecessary' for them.
        // I will remove them from here and place them in the specific test method.
    }

    @Test
    @DisplayName("1. CREATE: Should generate unique credentials (username/password) and set isActive=true.")
    void createProfile_GeneratesCredentialsAndSaves() {
        testLogger.info("Running Test: Creation and Credential Generation.");

        // ARRANGE: Stub generator and DAO within this specific test
        String MOCK_USERNAME = "jane.doe";
        when(generator.generateUsername(anyString(), anyString())).thenReturn(MOCK_USERNAME);
        String MOCK_PASSWORD = "testPassword123";
        when(generator.generatePassword()).thenReturn(MOCK_PASSWORD);

        Trainee savedTrainee = new Trainee();
        savedTrainee.setUserId(TEST_ID);
        when(traineeDao.save(any(Trainee.class))).thenReturn(savedTrainee);

        // ACT
        Trainee result = traineeService.createProfile(sampleTrainee);

        // ASSERT
        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeDao, times(1)).save(traineeCaptor.capture());

        Trainee capturedTrainee = traineeCaptor.getValue();

        assertEquals(MOCK_USERNAME, capturedTrainee.getUsername(), "Username must be set by generator.");
        assertEquals(MOCK_PASSWORD, capturedTrainee.getPassword(), "Password must be set by generator.");
        assertTrue(capturedTrainee.isActive(), "New profiles must be active.");
        assertEquals(TEST_ID, result.getUserId(), "Must return the Trainee object with the ID assigned by DAO.");

        testLogger.info("Test Passed: New Trainee profile created and credentials verified.");
    }

    @Test
    @DisplayName("2. UPDATE: Should pass existing Trainee object to DAO for modification.")
    void updateProfile_ExistingTraineeModified() {
        testLogger.info("Running Test: Profile Update.");
        // Note: This test does not use the 'generator' mock, so the stubbing must be moved.

        // ARRANGE: Set existing ID and modify a field
        sampleTrainee.setUserId(TEST_ID);
        sampleTrainee.setAddress("456 Updated St");

        // Mock the DAO to return the updated object
        when(traineeDao.save(any(Trainee.class))).thenReturn(sampleTrainee);

        // ACT
        Trainee result = traineeService.updateProfile(sampleTrainee);

        // ASSERT
        verify(traineeDao, times(1)).save(sampleTrainee);
        assertEquals(TEST_ID, result.getUserId(), "Updated trainee ID must match.");
        assertEquals("456 Updated St", result.getAddress(), "Address modification must persist.");

        testLogger.info("Test Passed: Update successful for Trainee ID {}", TEST_ID);
    }

    @Test
    @DisplayName("3. SELECT: Should return Optional empty when DAO returns nothing.")
    void selectProfile_NotFound() {
        testLogger.info("Running Test: Selection of non-existent Trainee.");
        // Note: This test does not use the 'generator' mock.

        // ARRANGE
        Long nonExistentId = 999L;
        when(traineeDao.findById(nonExistentId)).thenReturn(Optional.empty());

        // ACT
        Optional<Trainee> result = traineeService.selectProfile(nonExistentId);

        // ASSERT
        assertFalse(result.isPresent(), "Result should be empty for non-existent ID.");

        testLogger.info("Test Passed: Handled not found case correctly.");
    }

    @Test
    @DisplayName("4. DELETE: Should correctly call delete method on DAO.")
    void deleteProfile_Success() {
        testLogger.info("Running Test: Profile Deletion.");
        // Note: This test does not use the 'generator' mock.

        Long idToDelete = 10L;

        // ACT
        traineeService.deleteProfile(idToDelete);

        // ASSERT
        verify(traineeDao, times(1)).delete(idToDelete);

        testLogger.info("Test Passed: DAO delete method verified.");
    }
}