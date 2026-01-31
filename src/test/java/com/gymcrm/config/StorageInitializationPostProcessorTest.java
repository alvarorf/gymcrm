package com.gymcrm.config;

import com.gymcrm.util.DataLoader;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageInitializationPostProcessorTest {

    @Mock
    private DataLoader dataLoader;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @InjectMocks
    private StorageInitializationPostProcessor postProcessor;

    private final String testPath = "test-data.json";

    @BeforeEach
    void setUp() {
        // ARRANGE: Set the private @Value field using ReflectionTestUtils
        ReflectionTestUtils.setField(postProcessor, "dataPath", testPath);
    }

    @Test
    @DisplayName("1. INITIALIZE: Should trigger data load when bean is EntityManagerFactory")
    void shouldTriggerLoadingWhenEntityManagerFactoryDetected() {
        // ARRANGE
        String beanName = "entityManagerFactory";

        // ACT
        Object result = postProcessor.postProcessBeforeInitialization(entityManagerFactory, beanName);

        // ASSERT
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
        verify(dataLoader, never()).loadInitialData(anyString());
        assertSame(regularBean, result, "The post-processor should return the bean unmodified.");
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
        assertSame(bean, result);
    }
}