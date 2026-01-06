package com.gymcrm.config;

import com.gymcrm.util.DataLoader;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageInitializationPostProcessorTest {

    @Mock
    private DataLoader dataLoader;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @InjectMocks
    private StorageInitializationPostProcessor postProcessor;

    private final String testPath = "initial-data.json";

    @BeforeEach
    void setUp() {
        // ARRANGE
        ReflectionTestUtils.setField(postProcessor, "dataPath", testPath);
    }

    @Test
    @DisplayName("1. INITIALIZE: Should call dataLoader when EntityManagerFactory is initialized.")
    void postProcessAfterInitialization_shouldTriggerLoading() {
        // ARRANGE
        String beanName = "entityManagerFactory";

        // ACT
        postProcessor.postProcessAfterInitialization(entityManagerFactory, beanName);

        // ASSERT
        verify(dataLoader, times(1)).loadInitialData(testPath);
    }

    @Test
    @DisplayName("2. IGNORE: Should not call dataLoader for unrelated beans.")
    void postProcessAfterInitialization_shouldIgnoreOtherBeans() {
        // ARRANGE
        Object otherBean = new Object();

        // ACT
        postProcessor.postProcessAfterInitialization(otherBean, "someBean");

        // ASSERT
        verify(dataLoader, never()).loadInitialData(anyString());
    }
}