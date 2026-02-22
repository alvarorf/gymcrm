package com.gymcrm.core.util;

import com.gymcrm.core.util.CredentialsGenerator;
import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.GeneratedCredentialsResponse;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Credentials Generator Utility Tests")
class CredentialsGeneratorTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialsGenerator credentialsGenerator;

    @BeforeEach
    void setUp() {
        // Default behavior: username is available
        when(traineeDao.findByUsername(anyString())).thenReturn(Optional.empty());
        when(trainerDao.findByUsername(anyString())).thenReturn(Optional.empty());

        // Default behavior: "encode" just adds a prefix for testing
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
    }

    @Test
    @DisplayName("GENERATE: Should create base username when no collisions exist")
    void generate_BaseUsernameSuccess() {
        // ARRANGE
        String firstName = "John";
        String lastName = "Doe";

        // ACT
        GeneratedCredentialsResponse result = credentialsGenerator.generate(firstName, lastName);

        // ASSERT
        assertEquals("john.doe", result.username());
        assertEquals(10, result.rawPassword().length());
        assertTrue(result.encodedPassword().startsWith("encoded_"));
        verify(traineeDao).findByUsername("john.doe");
    }

    @Test
    @DisplayName("GENERATE: Should increment serial if username exists in TraineeDao")
    void generate_CollisionWithTrainee() {
        // ARRANGE
        // First call finds a trainee, second call finds nothing
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(new Trainee()));
        when(traineeDao.findByUsername("john.doe1")).thenReturn(Optional.empty());

        // ACT
        GeneratedCredentialsResponse result = credentialsGenerator.generate("John", "Doe");

        // ASSERT
        assertEquals("john.doe1", result.username());
        verify(traineeDao, times(2)).findByUsername(anyString());
    }

    @Test
    @DisplayName("GENERATE: Should increment serial if username exists in TrainerDao")
    void generate_CollisionWithTrainer() {
        // ARRANGE
        when(trainerDao.findByUsername("john.doe")).thenReturn(Optional.of(new Trainer()));

        // ACT
        GeneratedCredentialsResponse result = credentialsGenerator.generate("John", "Doe");

        // ASSERT
        assertEquals("john.doe1", result.username());
    }

    @Test
    @DisplayName("GENERATE: Should handle multiple collisions and keep incrementing")
    void generate_MultipleCollisions() {
        // ARRANGE
        when(traineeDao.findByUsername("john.doe")).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findByUsername("john.doe1")).thenReturn(Optional.of(new Trainer()));
        // john.doe2 remains empty (available)

        // ACT
        GeneratedCredentialsResponse result = credentialsGenerator.generate("John", "Doe");

        // ASSERT
        assertEquals("john.doe2", result.username());
    }

    @Test
    @DisplayName("GENERATE: Should remove spaces from names for username")
    void generate_NamesWithSpaces() {
        // ARRANGE
        String firstName = " John  ";
        String lastName = "  De  La Cruz ";

        // ACT
        GeneratedCredentialsResponse result = credentialsGenerator.generate(firstName, lastName);

        // ASSERT
        assertEquals("john.delacruz", result.username());
    }

    @Test
    @DisplayName("PASSWORD: Should generate unique passwords on every call")
    void generate_RandomPasswords() {
        // ACT
        GeneratedCredentialsResponse res1 = credentialsGenerator.generate("A", "B");
        GeneratedCredentialsResponse res2 = credentialsGenerator.generate("A", "B");

        // ASSERT
        assertNotEquals(res1.rawPassword(), res2.rawPassword(), "Passwords should be random");
    }
}