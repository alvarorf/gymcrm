package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.util.Nomenclature;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainer service unit tests")
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TraineeDao traineeDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks private TrainerServiceImpl trainerService;

    private Trainer sampleTrainer;
    private final String MOCK_USER = "dwight.schrute";
    private final String MOCK_PASS = "beetroot123";
    private final Long TEST_ID = 102L;

    @BeforeEach
    void setUp() {
        // Resolve Mockito setter injection issues
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setTraineeDao(traineeDao);

        TrainingType mockTrainingType = new TrainingType();
        mockTrainingType.setId(1L);
        mockTrainingType.setTrainingTypeName("Martial Arts");

        sampleTrainer = new Trainer();
        sampleTrainer.setUserId(TEST_ID);
        sampleTrainer.setFirstName("Dwight");
        sampleTrainer.setLastName("Schrute");
        sampleTrainer.setUsername(MOCK_USER);
        sampleTrainer.setPassword(MOCK_PASS);
        sampleTrainer.setSpecialization(mockTrainingType);
        sampleTrainer.setActive(true);
    }

    @Test
    @DisplayName("CREATE: Should set generated credentials and save trainer")
    void createProfile_Success() {
        // ARRANGE
        when(usernameGenerator.generateUsername("Dwight", "Schrute")).thenReturn(MOCK_USER);
        when(passwordGenerator.generatePassword()).thenReturn(MOCK_PASS);
        when(trainerDao.save(any(Trainer.class))).thenAnswer(i -> i.getArguments()[0]);

        // ACT
        Trainer result = trainerService.createProfile(sampleTrainer);

        // ASSERT
        assertAll("Verify profile creation",
                () -> assertEquals(MOCK_USER, result.getUsername()),
                () -> assertEquals(MOCK_PASS, result.getPassword()),
                () -> assertNotNull(result.getSpecialization()),
                () -> assertEquals("Martial Arts", result.getSpecialization().getTrainingTypeName())
        );
    }

    @Test
    @DisplayName("AUTHENTICATE: Should return true when credentials match")
    void authenticate_Success() {
        // ARRANGE
        when(trainerDao.findByUsername(MOCK_USER)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        boolean result = trainerService.authenticate(MOCK_USER, MOCK_PASS);

        // ASSERT
        assertTrue(result);
        verify(trainerDao, times(1)).findByUsername(MOCK_USER);
    }

    @Test
    @DisplayName("UPDATE: Should call DAO save for existing trainer")
    void updateProfile_Success() {
        // ARRANGE
        when(trainerDao.save(sampleTrainer)).thenReturn(sampleTrainer);

        // ACT
        Trainer result = trainerService.updateProfile(sampleTrainer);

        // ASSERT
        assertNotNull(result);
        verify(trainerDao, times(1)).save(sampleTrainer);
    }

    @Test
    @DisplayName("UPDATE FAILURE: Should throw IllegalArgumentException when first name or last name is missing")
    void updateProfile_MissingNames_ThrowsException() {
        // ARRANGE
        Trainer invalidTrainer = new Trainer();
        invalidTrainer.setFirstName(null); // Triggers the validation logic
        invalidTrainer.setLastName("Schrute");

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            trainerService.updateProfile(invalidTrainer));

        assertEquals(Nomenclature.MSG_REQUIRED, exception.getMessage());
        verify(trainerDao, never()).save(any(Trainer.class));
    }

    @Test
    @DisplayName("UPDATE FAILURE: Should throw IllegalArgumentException when last name is null")
    void updateProfile_LastNameNull_ThrowsException() {
        // ARRANGE
        Trainer invalidTrainer = new Trainer();
        invalidTrainer.setFirstName("Dwight");
        invalidTrainer.setLastName(null); // Triggers the second part of the || condition

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            trainerService.updateProfile(invalidTrainer));

        assertEquals(Nomenclature.MSG_REQUIRED, exception.getMessage());
        verify(trainerDao, never()).save(any(Trainer.class));
    }



    @Test
    @DisplayName("SELECT (ID): Should return Trainer when ID exists")
    void selectProfile_FoundById() {
        // ARRANGE
        when(trainerDao.findById(TEST_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        Optional<Trainer> result = trainerService.selectProfile(TEST_ID);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Dwight", result.get().getFirstName());
    }

    @Test
    @DisplayName("SELECT (USERNAME): Should return Trainer when username exists")
    void selectProfile_FoundByUsername() {
        // ARRANGE
        when(trainerDao.findByUsername(MOCK_USER)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        Optional<Trainer> result = trainerService.selectProfile(MOCK_USER);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(MOCK_USER, result.get().getUsername());
    }

    @Test
    @DisplayName("UPDATE PASSWORD: Should update trainer's password and save")
    void updatePassword_Success() {
        // ARRANGE
        String newPass = "moseIsTheBest";
        when(trainerDao.findById(TEST_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.updatePassword(TEST_ID, newPass);

        // ASSERT
        assertEquals(newPass, sampleTrainer.getPassword());
        verify(trainerDao, times(1)).save(sampleTrainer);
    }

    @Test
    @DisplayName("TOGGLE ACTIVATION: Should flip activation status")
    void toggleActivation_Success() {
        // ARRANGE
        sampleTrainer.setActive(true);
        when(trainerDao.findById(TEST_ID)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        trainerService.toggleActivation(TEST_ID);

        // ASSERT
        assertFalse(sampleTrainer.isActive());
        verify(trainerDao, times(1)).save(sampleTrainer);
    }

    @Test
    @DisplayName("GET UNASSIGNED: Should filter out trainers already assigned to the trainee")
    void getUnassignedTrainers_Success() {
        // ARRANGE
        String traineeUsername = "jim.halpert";

        // Setup existing trainer (Dwight) already assigned to Jim
        Trainee jim = new Trainee();
        jim.setUsername(traineeUsername);
        Set<Trainer> assignedTrainers = new HashSet<>();
        assignedTrainers.add(sampleTrainer); // Dwight is already assigned
        jim.setTrainers(assignedTrainers);

        // Set up a new trainer (Michael) who is unassigned
        Trainer unassignedTrainer = new Trainer();
        unassignedTrainer.setFirstName("Michael");
        unassignedTrainer.setUsername("michael.scott");

        List<Trainer> allTrainers = List.of(sampleTrainer, unassignedTrainer);

        when(traineeDao.findByUsername(traineeUsername)).thenReturn(Optional.of(jim));
        when(trainerDao.findAll()).thenReturn(allTrainers);

        // ACT
        List<Trainer> result = trainerService.getUnassignedTrainersByTraineeUsername(traineeUsername);

        // ASSERT
        assertEquals(1, result.size(), "Result should only contain the unassigned trainer");
        assertEquals("Michael", result.get(0).getFirstName());
        assertFalse(result.contains(sampleTrainer), "Assigned trainer should be filtered out");
    }

    @Test
    @DisplayName("GET UNASSIGNED: Should return all trainers if trainee's trainer set is null")
    void getUnassignedTrainers_NullTrainerSet_ReturnsAll() {
        // ARRANGE
        String traineeUsername = "pam.beesly";
        Trainee pam = new Trainee();
        pam.setUsername(traineeUsername);
        pam.setTrainers(null); // Triggers the 'trainee.getTrainers() == null' branch

        List<Trainer> allTrainers = List.of(sampleTrainer);

        when(traineeDao.findByUsername(traineeUsername)).thenReturn(Optional.of(pam));
        when(trainerDao.findAll()).thenReturn(allTrainers);

        // ACT
        List<Trainer> result = trainerService.getUnassignedTrainersByTraineeUsername(traineeUsername);

        // ASSERT
        assertEquals(1, result.size());
        assertTrue(result.contains(sampleTrainer));
        verify(trainerDao, times(1)).findAll();
    }
}