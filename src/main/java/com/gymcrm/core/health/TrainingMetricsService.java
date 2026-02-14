package com.gymcrm.core.health;

import com.gymcrm.core.util.Nomenclature;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class TrainingMetricsService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingMetricsService.class);

    private final MeterRegistry meterRegistry;
    private final Environment env;
    private Counter trainingCreationCounter;

    public TrainingMetricsService(MeterRegistry meterRegistry, Environment env) {
        this.meterRegistry = meterRegistry;
        this.env = env;
    }

    @PostConstruct
    public void init() {
        // Pull profiles programmatically
        String activeProfile = Arrays.stream(env.getActiveProfiles())
                .findFirst()
                .orElse("default");

        Nomenclature.info(logger, Nomenclature.Action.METRICS_INIT, activeProfile);

        // Define a Counter with the dynamic environment tag
        this.trainingCreationCounter = Counter.builder("gymcrm.trainings.created.total")
                .description("Total number of trainings created since startup")
                .tag("environment", activeProfile)
                .register(meterRegistry);

        Nomenclature.success(logger, Nomenclature.Action.METRICS_INIT, activeProfile);
    }

    public void incrementTrainingCount() {
        this.trainingCreationCounter.increment();
    }
}