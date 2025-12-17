package com.gymcrm.util;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;

    @InjectMocks private UsernameGenerator usernameGenerator;

    @Test
    @DisplayName("Should return base username when no duplicates exist")
    void generateUsername_NoDuplicates() {
        // ARRANGE
        String first = "John";
        String last = "Doe";
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("john.doe")).thenReturn(Optional.empty());

        // ACT
        String result = usernameGenerator.generateUsername(first, last);

        // ASSERT
        assertEquals("john.doe", result);
    }

    @Test
    @DisplayName("Should append serial number 1 when base username is taken")
    void generateUsername_WithDuplicate() {
        // ARRANGE
        String first = "John";
        String last = "Doe";
        // Simulate 'john.doe' already exists in Trainee DAO
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(new Trainee()));
        // Ensure 'john.doe1' is free
        when(traineeDao.findByUsername("john.doe1")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("john.doe1")).thenReturn(Optional.empty());

        // ACT
        String result = usernameGenerator.generateUsername(first, last);

        // ASSERT
        assertEquals("john.doe1", result);
    }
}