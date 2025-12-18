package com.gymcrm.dao;

import com.gymcrm.model.Trainee;
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
class TraineeDaoImplTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TraineeDaoImpl traineeDao;

    private Map<Long, Trainee> traineeMap;

    @BeforeEach
    void setUp() {
        traineeMap = new HashMap<>();
        lenient().when(storage.getTraineeStorageMap()).thenReturn(traineeMap);
    }

    @Test
    @DisplayName("1. SAVE: Should generate ID when userId is null.")
    void save_shouldGenerateId_whenIdIsNull() {
        // ARRANGE
        Trainee trainee = Trainee.builder().firstName("John").lastName("Doe").build();
        when(storage.getNextTraineeId()).thenReturn(1L);

        // ACT
        Trainee savedTrainee = traineeDao.save(trainee);

        // ASSERT
        assertNotNull(savedTrainee.getUserId());
        assertEquals(1L, savedTrainee.getUserId());
        assertTrue(traineeMap.containsKey(1L));
        verify(storage, times(1)).getNextTraineeId();
    }

    @Test
    @DisplayName("2. SELECT: Should return Trainee when ID exists.")
    void findById_shouldReturnTrainee_whenExists() {
        // ARRANGE
        Long id = 1L;
        Trainee trainee = Trainee.builder().userId(id).username("john.doe").build();
        traineeMap.put(id, trainee);

        // ACT
        Optional<Trainee> result = traineeDao.findById(id);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUsername());
    }

    @Test
    @DisplayName("3. SELECT ALL: Should return all Trainees from storage.")
    void findAll_shouldReturnAllTrainees() {
        // ARRANGE
        traineeMap.put(1L, Trainee.builder().userId(1L).build());
        traineeMap.put(2L, Trainee.builder().userId(2L).build());

        // ACT
        List<Trainee> result = traineeDao.findAll();

        // ASSERT
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("4. DELETE: Should remove trainee from storage map.")
    void delete_shouldRemoveTraineeFromStorage() {
        // ARRANGE
        Long id = 1L;
        traineeMap.put(id, Trainee.builder().userId(id).build());

        // ACT
        traineeDao.delete(id);

        // ASSERT
        assertFalse(traineeMap.containsKey(id));
    }

    @Test
    @DisplayName("5. SELECT BY USERNAME: Should return Trainee when username matches.")
    void findByUsername_shouldReturnTrainee_whenUsernameMatches() {
        // ARRANGE
        String username = "test.user";
        Trainee trainee = Trainee.builder().userId(1L).username(username).build();
        traineeMap.put(1L, trainee);

        // ACT
        Optional<Trainee> result = traineeDao.findByUsername(username);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }
}