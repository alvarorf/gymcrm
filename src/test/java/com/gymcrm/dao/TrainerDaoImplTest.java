package com.gymcrm.dao;

import com.gymcrm.model.Trainer;
import com.gymcrm.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerDaoImplTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    private Map<Long, Trainer> trainerMap;

    @BeforeEach
    void setUp() {
        trainerMap = new HashMap<>();
        lenient().when(storage.getTrainerStorageMap()).thenReturn(trainerMap);
    }

    @Test
    @DisplayName("1. SAVE: Should generate a new ID and save when userId is null.")
    void save_shouldGenerateId_whenUserIdIsNull() {
        // ARRANGE
        Trainer trainer = Trainer.builder().firstName("Michael").lastName("Scott").build();
        when(storage.getNextTrainerId()).thenReturn(101L);

        // ACT
        Trainer savedTrainer = trainerDao.save(trainer);

        // ASSERT
        assertNotNull(savedTrainer.getUserId());
        assertEquals(101L, savedTrainer.getUserId());
        assertTrue(trainerMap.containsKey(101L));
        verify(storage, times(1)).getNextTrainerId();
    }

    @Test
    @DisplayName("2. SAVE: Should update existing trainer and not generate ID when userId is provided.")
    void save_shouldUseExistingId_whenIdIsProvided() {
        // ARRANGE
        Trainer trainer = Trainer.builder().userId(101L).firstName("Michael").build();

        // ACT
        Trainer savedTrainer = trainerDao.save(trainer);

        // ASSERT
        assertEquals(101L, savedTrainer.getUserId());
        assertTrue(trainerMap.containsKey(101L));
        verify(storage, never()).getNextTrainerId();
    }

    @Test
    @DisplayName("3. SELECT: Should return Optional containing Trainer when ID exists.")
    void findById_shouldReturnTrainer_whenExists() {
        // ARRANGE
        Long id = 101L;
        Trainer trainer = Trainer.builder().userId(id).firstName("Michael").build();
        trainerMap.put(id, trainer);

        // ACT
        Optional<Trainer> result = trainerDao.findById(id);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Michael", result.get().getFirstName());
    }

    @Test
    @DisplayName("4. SELECT: Should return Optional empty when ID does not exist.")
    void findById_shouldReturnEmpty_whenNotExists() {
        // ARRANGE
        Long id = 999L;

        // ACT
        Optional<Trainer> result = trainerDao.findById(id);

        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("5. SELECT ALL: Should return a list containing all trainers in storage.")
    void findAll_shouldReturnAllTrainers() {
        // ARRANGE
        trainerMap.put(101L, Trainer.builder().userId(101L).build());
        trainerMap.put(102L, Trainer.builder().userId(102L).build());

        // ACT
        List<Trainer> result = trainerDao.findAll();

        // ASSERT
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getUserId().equals(101L)));
    }

    @Test
    @DisplayName("6. SELECT BY USERNAME: Should return Trainer when username matches.")
    void findByUsername_shouldReturnTrainer_whenUsernameMatches() {
        // ARRANGE
        String username = "michael.scott";
        Trainer trainer = Trainer.builder().userId(101L).username(username).build();
        trainerMap.put(101L, trainer);

        // ACT
        Optional<Trainer> result = trainerDao.findByUsername(username);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }
}