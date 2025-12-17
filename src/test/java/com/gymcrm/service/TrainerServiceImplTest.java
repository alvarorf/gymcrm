package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainer service unit tests")
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks private TrainerServiceImpl trainerService;

    private Trainer sampleTrainer;
    private final String MOCK_USER = "dwight.schrute";
    private final String MOCK_PASS = "beetroot123";

    @BeforeEach
    void setUp() {
        // Resolve Mockito setter injection issue
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);

        sampleTrainer = new Trainer();
        sampleTrainer.setFirstName("Dwight");
        sampleTrainer.setLastName("Schrute");
        sampleTrainer.setSpecialization("Martial Arts");
    }

    @Test
    @DisplayName("CREATE: Should set generated credentials and save trainer")
    void createProfile_Success() {
        // ARRANGE
        when(usernameGenerator.generateUsername("Dwight", "Schrute")).thenReturn(MOCK_USER);
        when(passwordGenerator.generatePassword()).thenReturn(MOCK_PASS);
        when(trainerDao.save(any(Trainer.class))).thenReturn(sampleTrainer);

        // ACT
        Trainer result = trainerService.createProfile(sampleTrainer);

        // ASSERT
        assertAll("Verify profile creation",
                () -> assertEquals(MOCK_USER, result.getUsername()),
                () -> assertEquals(MOCK_PASS, result.getPassword()),
                () -> verify(trainerDao, times(1)).save(sampleTrainer)
        );
    }

    @Test
    @DisplayName("UPDATE: Should call DAO save for existing trainer")
    void updateProfile_Success() {
        // ARRANGE
        sampleTrainer.setUserId(102L);
        when(trainerDao.save(sampleTrainer)).thenReturn(sampleTrainer);

        // ACT
        Trainer result = trainerService.updateProfile(sampleTrainer);

        // ASSERT
        assertNotNull(result);
        verify(trainerDao, times(1)).save(sampleTrainer);
    }

    @Test
    @DisplayName("SELECT: Should return Trainer when ID exists")
    void selectProfile_Found() {
        // ARRANGE
        Long testId = 102L;
        when(trainerDao.findById(testId)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        Optional<Trainer> result = trainerService.selectProfile(testId);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Dwight", result.get().getFirstName());
    }
}