package com.gymcrm.dao;

import com.gymcrm.model.Training;
import com.gymcrm.repositories.TrainingRepository;
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
@DisplayName("Training DAO Unit Tests")
class TrainingDaoImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingDaoImpl trainingDao;

    @Test
    @DisplayName("1. SAVE: Should persist training using repository")
    void save_shouldCallRepository() {
        // ARRANGE
        Training training = new Training();
        training.setTrainingName("Yoga Morning");
        when(trainingRepository.save(training)).thenReturn(training);

        // ACT
        Training result = trainingDao.save(training);

        // ASSERT
        assertNotNull(result);
        assertEquals("Yoga Morning", result.getTrainingName());
        verify(trainingRepository, times(1)).save(training);
    }

    @Test
    @DisplayName("2. FIND BY NAME: Should return training when name matches")
    void findByName_shouldReturnOptional() {
        // ARRANGE
        String name = "Power Lifting";
        Training training = new Training();
        training.setTrainingName(name);
        when(trainingRepository.findByTrainingName(name)).thenReturn(Optional.of(training));

        // ACT
        Optional<Optional<Training>> result = Optional.ofNullable(trainingDao.findByName(name));

        // ASSERT
        assertTrue(result.get().isPresent());
        assertEquals(name, result.get().get().getTrainingName());
        verify(trainingRepository, times(1)).findByTrainingName(name);
    }

    @Test
    @DisplayName("3. FIND ALL: Should return list of all trainings")
    void findAll_shouldReturnList() {
        // ARRANGE
        List<Training> trainings = List.of(new Training(), new Training());
        when(trainingRepository.findAll()).thenReturn(trainings);

        // ACT
        List<Training> result = trainingDao.findAll();

        // ASSERT
        assertEquals(2, result.size());
        verify(trainingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("4. FIND BY ID: Should return training when ID exists in repository")
    void findById_shouldReturnTraining() {
        // ARRANGE
        Long id = 500L;
        Training training = new Training();
        training.setId(id);
        when(trainingRepository.findById(id)).thenReturn(Optional.of(training));

        // ACT
        Optional<Training> result = trainingDao.findById(id);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(trainingRepository, times(1)).findById(id);
    }
}