package com.gymcrm.util;

import com.gymcrm.core.util.DataLoader;
import com.gymcrm.repositories.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataLoaderTest {

    private DataLoader dataLoader;

    @Mock private TraineeRepository traineeRepository;
    @Mock private TrainerRepository trainerRepository;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // ARRANGE
        dataLoader =
                new DataLoader(traineeRepository, trainerRepository, trainingRepository, trainingTypeRepository, passwordEncoder);

        // Mock password encoding behavior
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }

    @Test
    @DisplayName("1. LOAD: Should persist all entities to DB when valid JSON path is provided.")
    void loadInitialData_shouldPersistToAllRepositories() {
        // ARRANGE
        String validPath = "initial-data/initial-data.json";

        // ACT
        dataLoader.loadInitialData(validPath);

        // ASSERT
        // Verify that each repository saveAll method was called
        verify(trainingTypeRepository, times(1)).saveAll(anyList());
        verify(trainerRepository, times(1)).saveAll(anyList());
        verify(traineeRepository, times(1)).saveAll(anyList());
        verify(trainingRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("2. ERROR: Should not interact with repositories when file path is invalid.")
    void loadInitialData_shouldHandleFileNotFound() {
        // ARRANGE
        String invalidPath = "invalid/path.json";

        // ACT
        dataLoader.loadInitialData(invalidPath);

        // ASSERT
        // Verify no save operations were attempted
        verify(trainingTypeRepository, never()).saveAll(anyList());
        verify(trainerRepository, never()).saveAll(anyList());
        verify(traineeRepository, never()).saveAll(anyList());
        verify(trainingRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("3. NULL BRANCHES: Should not call repositories when JSON contains empty object {}")
    void loadInitialData_shouldSkipNullCollections() throws Exception {
        // ARRANGE
        // Create an InputStream that represents an empty JSON object "{}"
        java.io.InputStream emptyJsonStream = new java.io.ByteArrayInputStream("{}".getBytes());

        // To mock the internal 'new ClassPathResource', we would typically need Mockito-Inline for constructor mocking.
        // A simpler way without extra dependencies is to point to a path that returns our mocked stream, or use Mockito.mockConstruction.

        try (var mockedResource = mockConstruction(org.springframework.core.io.ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream()).thenReturn(emptyJsonStream);
                })) {

            // ACT
            dataLoader.loadInitialData("any-path.json");

            // ASSERT
            // Verify no saveAll calls were made because DataWrapper fields will be null
            verify(trainingTypeRepository, never()).saveAll(anyList());
            verify(trainerRepository, never()).saveAll(anyList());
            verify(traineeRepository, never()).saveAll(anyList());
            verify(trainingRepository, never()).saveAll(anyList());
        }
    }
}