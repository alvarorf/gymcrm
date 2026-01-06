package com.gymcrm.dao;

import com.gymcrm.model.Trainer;
import com.gymcrm.repositories.TrainerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Trainer DAO Unit Tests")
class TrainerDaoImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TrainerDaoImpl trainerDao;

    @Test
    @DisplayName("1. SAVE: Should call repository save method and return saved trainer.")
    void save_shouldPersistTrainer() {
        // ARRANGE
        Trainer trainer = Trainer.builder()
                .firstName("Michael")
                .lastName("Scott")
                .build();

        // Mocking behavior: JPA repositories return the saved object (usually with an ID)
        Trainer savedTrainer = Trainer.builder()
                .userId(101L)
                .firstName("Michael")
                .lastName("Scott")
                .build();

        when(trainerRepository.save(trainer)).thenReturn(savedTrainer);

        // ACT
        Trainer result = trainerDao.save(trainer);

        // ASSERT
        assertNotNull(result);
        assertEquals(101L, result.getUserId());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    @DisplayName("2. SELECT: Should return Optional containing Trainer when ID exists.")
    void findById_shouldReturnTrainer_whenExists() {
        // ARRANGE
        Long id = 101L;
        Trainer trainer = Trainer.builder().userId(id).firstName("Michael").build();
        when(trainerRepository.findById(id)).thenReturn(Optional.of(trainer));

        // ACT
        Optional<Trainer> result = trainerDao.findById(id);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Michael", result.get().getFirstName());
        verify(trainerRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("3. SELECT: Should return empty Optional when ID does not exist.")
    void findById_shouldReturnEmpty_whenNotExists() {
        // ARRANGE
        Long id = 999L;
        when(trainerRepository.findById(id)).thenReturn(Optional.empty());

        // ACT
        Optional<Trainer> result = trainerDao.findById(id);

        // ASSERT
        assertTrue(result.isEmpty());
        verify(trainerRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("4. SELECT ALL: Should return a list of all trainers from the repository.")
    void findAll_shouldReturnAllTrainers() {
        // ARRANGE
        List<Trainer> trainers = List.of(
                Trainer.builder().userId(101L).build(),
                Trainer.builder().userId(102L).build()
        );
        when(trainerRepository.findAll()).thenReturn(trainers);

        // ACT
        List<Trainer> result = trainerDao.findAll();

        // ASSERT
        assertEquals(2, result.size());
        verify(trainerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("5. SELECT BY USERNAME: Should return Trainer when username matches.")
    void findByUsername_shouldReturnTrainer_whenUsernameMatches() {
        // ARRANGE
        String username = "michael.scott";
        Trainer trainer = Trainer.builder().userId(101L).username(username).build();
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));

        // ACT
        Optional<Trainer> result = trainerDao.findByUsername(username);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(trainerRepository, times(1)).findByUsername(username);
    }
}