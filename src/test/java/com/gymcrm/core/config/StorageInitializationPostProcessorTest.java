package com.gymcrm.core.config;

import com.gymcrm.core.util.DataLoader;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageInitializationPostProcessorTest {

    @Mock
    private DataLoader dataLoader;

    @Mock
    private Environment env; // Mock the Environment instead of using Reflection

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @InjectMocks
    private StorageInitializationPostProcessor postProcessor;

    private final String testPath = "initial-data/initial-dev-data.json";

    @Test
    @DisplayName("1. INITIALIZE: Should trigger data load when bean is EntityManagerFactory")
    void shouldTriggerLoadingWhenEntityManagerFactoryDetected() {
        // ARRANGE
        String beanName = "entityManagerFactory";
        // Stub the environment to return our test path
        when(env.getProperty("storage.initial-data-file")).thenReturn(testPath);

        // ACT
        Object result = postProcessor.postProcessBeforeInitialization(entityManagerFactory, beanName);

        // ASSERT
        verify(env).getProperty("storage.initial-data-file");
        verify(dataLoader, times(1)).loadInitialData(testPath);
        assertSame(entityManagerFactory, result, "The post-processor should return the bean unmodified.");
    }

    @Test
    @DisplayName("2. IGNORE: Should not trigger data load for non-EntityManagerFactory beans")
    void shouldNotTriggerLoadingForOtherBeans() {
        // ARRANGE
        Object regularBean = new Object();
        String beanName = "someServiceBean";

        // ACT
        Object result = postProcessor.postProcessBeforeInitialization(regularBean, beanName);

        // ASSERT
        // Should not even check the environment if it's not an EntityManagerFactory
        verifyNoInteractions(env);
        verify(dataLoader, never()).loadInitialData(anyString());
        assertSame(regularBean, result);
    }

    @Test
    @DisplayName("3. PASS-THROUGH: postProcessAfterInitialization should return bean without interaction")
    void postProcessAfterInitializationShouldDoNothing() {
        // ARRANGE
        Object bean = new Object();

        // ACT
        Object result = postProcessor.postProcessAfterInitialization(bean, "anyName");

        // ASSERT
        verifyNoInteractions(dataLoader);
        verifyNoInteractions(env);
        assertSame(bean, result);
    }
}