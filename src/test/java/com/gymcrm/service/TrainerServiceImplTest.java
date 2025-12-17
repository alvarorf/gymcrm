package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.util.UsernamePasswordGenerator;
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

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernamePasswordGenerator generator;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer sampleTrainer;
    private final String MOCK_USER = "dwight.schrute";
    private final String MOCK_PASS = "beetroot123";

    @BeforeEach
    void setUp() {
        // Fix for setter injection issue in Mockito
        trainerService.setGenerator(generator);

        sampleTrainer = new Trainer();
        sampleTrainer.setFirstName("Dwight");
        sampleTrainer.setLastName("Schrute");
        sampleTrainer.setSpecialization("Martial Arts");
    }

    @Test
    @DisplayName("CREATE: Should set generated credentials and save trainer")
    void createProfile_Success() {
        // ARRANGE
        when(generator.generateUsername("Dwight", "Schrute")).thenReturn(MOCK_USER);
        when(generator.generatePassword()).thenReturn(MOCK_PASS);
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
        Long id = 102L;
        when(trainerDao.findById(id)).thenReturn(Optional.of(sampleTrainer));

        // ACT
        Optional<Optional<Trainer>> result = Optional.ofNullable(trainerService.selectProfile(id));

        // ASSERT
        assertTrue(result.get().isPresent());
        assertEquals("Dwight", result.get().get().getFirstName());
    }
}
