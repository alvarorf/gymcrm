package com.gymcrm.service;

import com.gymcrm.model.TrainingType;
import com.gymcrm.repositories.TrainingTypeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Training Type Service Unit Tests")
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    @DisplayName("GET ALL: Should return a list of all available training types")
    void getAllTrainingTypes_ReturnsList() {
        // ARRANGE
        TrainingType cardio = TrainingType.builder().trainingTypeName("Cardio").build();
        TrainingType yoga = TrainingType.builder().trainingTypeName("Yoga").build();
        when(trainingTypeRepository.findAll()).thenReturn(List.of(cardio, yoga));

        // ACT
        List<TrainingType> result = trainingTypeService.getAllTrainingTypes();

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cardio", result.get(0).getTrainingTypeName());
        verify(trainingTypeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("FIND BY NAME: Should return Optional containing TrainingType when name exists")
    void findByName_Found() {
        // ARRANGE
        String typeName = "Strength";
        TrainingType strength = TrainingType.builder().trainingTypeName(typeName).build();
        when(trainingTypeRepository.findByTrainingTypeName(typeName)).thenReturn(Optional.of(strength));

        // ACT
        Optional<TrainingType> result = trainingTypeService.findByName(typeName);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(typeName, result.get().getTrainingTypeName());
        verify(trainingTypeRepository).findByTrainingTypeName(typeName);
    }

    @Test
    @DisplayName("FIND BY NAME: Should return empty Optional when name does not exist")
    void findByName_NotFound() {
        // ARRANGE
        String typeName = "NonExistent";
        when(trainingTypeRepository.findByTrainingTypeName(typeName)).thenReturn(Optional.empty());

        // ACT
        Optional<TrainingType> result = trainingTypeService.findByName(typeName);

        // ASSERT
        assertFalse(result.isPresent());
        verify(trainingTypeRepository).findByTrainingTypeName(typeName);
    }
}