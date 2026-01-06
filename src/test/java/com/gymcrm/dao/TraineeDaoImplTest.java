package com.gymcrm.dao;

import com.gymcrm.model.Trainee;
import com.gymcrm.repositories.TraineeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    @Test
    @DisplayName("1. SAVE: Should return saved trainee with database-generated ID.")
    void save_shouldReturnSavedTrainee() {
        // ARRANGE
        Trainee trainee = Trainee.builder().firstName("John").lastName("Doe").build();
        Trainee savedTrainee = Trainee.builder().userId(1L).firstName("John").lastName("Doe").build();

        when(traineeRepository.save(trainee)).thenReturn(savedTrainee);

        // ACT
        Trainee result = traineeDao.save(trainee);

        // ASSERT
        assertNotNull(result.getUserId());
        assertEquals(1L, result.getUserId());
        verify(traineeRepository, times(1)).save(trainee);
    }

    @Test
    @DisplayName("2. SELECT: Should delegate to repository findById.")
    void findById_shouldReturnTrainee_whenExists() {
        // ARRANGE
        Long id = 1L;
        Trainee trainee = Trainee.builder().userId(id).username("john.doe").build();
        when(traineeRepository.findById(id)).thenReturn(Optional.of(trainee));

        // ACT
        Optional<Trainee> result = traineeDao.findById(id);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUsername());
        verify(traineeRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("3. SELECT ALL: Should return all Trainees from repository.")
    void findAll_shouldReturnAllTrainees() {
        // ARRANGE
        List<Trainee> trainees = Arrays.asList(new Trainee(), new Trainee());
        when(traineeRepository.findAll()).thenReturn(trainees);

        // ACT
        List<Trainee> result = traineeDao.findAll();

        // ASSERT
        assertEquals(2, result.size());
        verify(traineeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("4. DELETE: Should call repository deleteById.")
    void delete_shouldCallRepositoryDelete() {
        // ARRANGE
        Long id = 1L;

        // ACT
        traineeDao.delete(id);

        // ASSERT
        verify(traineeRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("5. SELECT BY USERNAME: Should call repository findByUsername.")
    void findByUsername_shouldReturnTrainee() {
        // ARRANGE
        String username = "test.user";
        Trainee trainee = Trainee.builder().userId(1L).username(username).build();
        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));

        // ACT
        Optional<Trainee> result = traineeDao.findByUsername(username);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(traineeRepository, times(1)).findByUsername(username);
    }
}