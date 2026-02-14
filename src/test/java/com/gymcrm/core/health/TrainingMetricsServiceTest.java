package com.gymcrm.core.health;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingMetricsServiceTest {

    private TrainingMetricsService metricsService;
    private MeterRegistry meterRegistry;
    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        // ARRANGE - Setup real SimpleMeterRegistry and MockEnvironment
        meterRegistry = new SimpleMeterRegistry();
        environment = new MockEnvironment();
        metricsService = new TrainingMetricsService(meterRegistry, environment);
    }

    @Test
    @DisplayName("Should initialize counter with 'dev' tag when 'dev' profile is active")
    void init_WithDevProfile_RegistersCounterWithCorrectTag() {
        // ARRANGE
        environment.setActiveProfiles("dev");

        // ACT
        metricsService.init();

        // ASSERT
        // Retrieve the counter from the registry to verify it was registered correctly
        Counter counter = meterRegistry.find("gymcrm.trainings.created.total")
                .tag("environment", "dev")
                .counter();

        assertEquals(0, counter.count(), "Initial count should be 0");
    }

    @Test
    @DisplayName("Should increment counter value when incrementTrainingCount is called")
    void incrementTrainingCount_IncreasesValue() {
        // ARRANGE
        environment.setActiveProfiles("prod");
        metricsService.init();
        Counter counter = meterRegistry.find("gymcrm.trainings.created.total").counter();

        // ACT
        metricsService.incrementTrainingCount();
        metricsService.incrementTrainingCount();

        // ASSERT
        assertEquals(2.0, counter.count(), "Counter should reflect two increments");
    }
}